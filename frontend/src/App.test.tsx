import { render, screen } from "@testing-library/react";
import { expect, it } from "vitest";
import "@testing-library/jest-dom/vitest";
import App from "./App";

it("muestra el título", () => {
  render(<App />);
  expect(screen.getByRole("heading", { name: "The Last Shipbender" })).toBeInTheDocument();
});
