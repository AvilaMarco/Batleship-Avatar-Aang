// Único archivo que toca web storage (ADR 006 U2): sessionStorage en dev, localStorage en prod.
const KEY = "shipbender.token";
const store = import.meta.env.DEV ? sessionStorage : localStorage;

export const getToken = (): string | null => store.getItem(KEY);
export const setToken = (t: string): void => store.setItem(KEY, t);
export const clearToken = (): void => store.removeItem(KEY);
