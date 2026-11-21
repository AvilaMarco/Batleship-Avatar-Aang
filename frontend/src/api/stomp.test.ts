import { beforeEach, describe, expect, it, vi } from "vitest";

const h = vi.hoisted(() => ({ cfg: null as any, client: null as any, subscribe: vi.fn(), publish: vi.fn(), deactivate: vi.fn() }));
vi.mock("@stomp/stompjs", () => ({
  Client: class {
    constructor(cfg: any) {
      h.cfg = cfg;
      h.client = this;
      Object.assign(this, cfg); // como el Client real: cfg pasa a ser propiedades
    }
    subscribe = h.subscribe;
    publish = h.publish;
    activate = vi.fn();
    deactivate = h.deactivate;
  },
}));

import { getToken, setToken } from "./auth";
import { connectGame } from "./stomp";

beforeEach(() => {
  vi.clearAllMocks();
  sessionStorage.clear();
  localStorage.clear();
  setToken("Bearer tok");
});

describe("connectGame", () => {
  it("conecta con Authorization y se suscribe a los 2 canales en cada onConnect", () => {
    const onConnect = vi.fn();
    connectGame(7, { onEvent: vi.fn(), onError: vi.fn(), onConnect });
    expect(h.cfg.connectHeaders.Authorization).toBe("Bearer tok");
    expect(h.cfg.heartbeatIncoming).toBe(10000);
    expect(h.cfg.heartbeatOutgoing).toBe(10000);
    expect(h.cfg.reconnectDelay).toBe(5000);
    expect(h.cfg.brokerURL).toMatch(/^wss?:\/\/.+\/the-last-shipbender$/);
    h.cfg.onConnect();
    h.cfg.onConnect(); // reconexión
    expect(h.subscribe.mock.calls.map((c) => c[0])).toEqual([
      "/topic/games/7", "/user/queue/errors", "/topic/games/7", "/user/queue/errors",
    ]);
    expect(onConnect).toHaveBeenCalledTimes(2);
  });

  it("parsea eventos y errores", () => {
    const onEvent = vi.fn();
    const onError = vi.fn();
    connectGame(7, { onEvent, onError, onConnect: vi.fn() });
    h.cfg.onConnect();
    h.subscribe.mock.calls[0][1]({ body: '{"type":"GAME_STARTED","game_id":7,"turn":1}' });
    h.subscribe.mock.calls[1][1]({ body: '{"code":"FORBIDDEN","status":403}' });
    expect(onEvent).toHaveBeenCalledWith({ type: "GAME_STARTED", game_id: 7, turn: 1 });
    expect(onError).toHaveBeenCalledWith(expect.objectContaining({ code: "FORBIDDEN" }));
  });

  it("beforeConnect relee el token en cada intento", () => {
    connectGame(7, { onEvent: vi.fn(), onError: vi.fn(), onConnect: vi.fn() });
    setToken("Bearer nuevo");
    h.cfg.beforeConnect();
    expect(h.client.connectHeaders.Authorization).toBe("Bearer nuevo");
  });

  it("onStompError limpia el token, desactiva y va a login", () => {
    location.hash = "#/games/7";
    connectGame(7, { onEvent: vi.fn(), onError: vi.fn(), onConnect: vi.fn() });
    h.cfg.onStompError({ headers: { message: "x" }, body: "" });
    expect(getToken()).toBeNull();
    expect(h.deactivate).toHaveBeenCalled();
    expect(location.hash).toBe("#/login");
  });

  it("un cierre sin ERROR no limpia nada", () => {
    connectGame(7, { onEvent: vi.fn(), onError: vi.fn(), onConnect: vi.fn() });
    h.cfg.onWebSocketClose?.();
    expect(getToken()).toBe("Bearer tok");
    expect(h.deactivate).not.toHaveBeenCalled();
  });

  it("sendEmote publica JSON y disconnect desactiva", () => {
    const c = connectGame(7, { onEvent: vi.fn(), onError: vi.fn(), onConnect: vi.fn() });
    c.sendEmote("emote-1");
    expect(h.publish).toHaveBeenCalledWith({
      destination: "/app/games/7/emotes",
      body: '{"emote":"emote-1"}',
      headers: { "content-type": "application/json" },
    });
    c.disconnect();
    expect(h.deactivate).toHaveBeenCalled();
  });
});
