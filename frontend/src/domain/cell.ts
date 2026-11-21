export type CellId = string; // "A1".."J10"
export const ROWS = "ABCDEFGHIJ";
export const COLS = 10;
export const isCell = (s: string): boolean => /^[A-J]([1-9]|10)$/.test(s);
