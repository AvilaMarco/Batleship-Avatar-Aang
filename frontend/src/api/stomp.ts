import { Client } from "@stomp/stompjs";
import { navigate } from "../router";
import { clearToken, getToken } from "./auth";
import type { GameEvent, ProblemDetail } from "./types";

export interface GameHandlers {
  onEvent: (e: GameEvent) => void;
  onError: (p: ProblemDetail) => void;
  onConnect: () => void;
}

export function connectGame(gameId: number, h: GameHandlers) {
  const proto = location.protocol === "https:" ? "wss" : "ws";
  const client = new Client({
    brokerURL: `${proto}://${location.host}/the-last-shipbender`,
    connectHeaders: { Authorization: getToken() ?? "" },
    // relee el token en cada intento (puede haber cambiado desde el primer CONNECT)
    beforeConnect: () => {
      client.connectHeaders = { Authorization: getToken() ?? "" };
    },
    // CONNECT rechazado (token caducado): no reintentar, volver al login. Una caída de red no pasa por aquí.
    onStompError: () => {
      void client.deactivate();
      clearToken();
      navigate("#/login");
    },
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(`/topic/games/${gameId}`, (m) => h.onEvent(JSON.parse(m.body)));
      client.subscribe("/user/queue/errors", (m) => h.onError(JSON.parse(m.body)));
      h.onConnect();
    },
  });
  client.activate();
  return {
    sendEmote: (emote: string) =>
      client.publish({
        destination: `/app/games/${gameId}/emotes`,
        body: JSON.stringify({ emote }),
        headers: { "content-type": "application/json" },
      }),
    disconnect: () => void client.deactivate(),
  };
}
