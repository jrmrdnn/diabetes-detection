import { vi } from "vitest";

Object.defineProperty(window, "requestAnimationFrame", {
  value: vi.fn((cb) => setTimeout(cb, 0)),
});

Object.defineProperty(window, "DOMParser", {
  value: class DOMParser {
    parseFromString(str: string, type: string) {
      const div = document.createElement("div");
      div.innerHTML = str;
      return {
        body: {
          firstChild: div.firstElementChild,
        },
      };
    }
  },
});

delete (window as any).location;
window.location = {
  href: "",
  assign: vi.fn(),
  replace: vi.fn(),
  reload: vi.fn(),
} as any;
