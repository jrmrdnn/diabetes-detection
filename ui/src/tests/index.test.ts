import {describe, expect, it} from "vitest";
import {User} from "../user";
import {Sort} from "../sort";
import {Search} from "../search";
import {Timeline} from "../timeline";
import {Pagination} from "../pagination";

describe("index.ts module", () => {
    it("defines custom elements with correct classes", async () => {
        await import("../index");
        expect(customElements.get("user-component")).toBe(User);
        expect(customElements.get("sort-component")).toBe(Sort);
        expect(customElements.get("search-component")).toBe(Search);
        expect(customElements.get("timeline-component")).toBe(Timeline);
        expect(customElements.get("pagination-component")).toBe(Pagination);
    });

    it("does not throw when redefining custom elements", async () => {
        await import("../index");
        expect(() => {
            customElements.define("user-component", User);
        }).toThrow();
        expect(() => {
            customElements.define("sort-component", Sort);
        }).toThrow();
        expect(() => {
            customElements.define("search-component", Search);
        }).toThrow();
        expect(() => {
            customElements.define("timeline-component", Timeline);
        }).toThrow();
        expect(() => {
            customElements.define("pagination-component", Pagination);
        }).toThrow();
    });

    it("imports all modules without undefined", () => {
        expect(User).toBeDefined();
        expect(Sort).toBeDefined();
        expect(Search).toBeDefined();
        expect(Timeline).toBeDefined();
        expect(Pagination).toBeDefined();
    });

    it("does not throw on import", async () => {
        await expect(import("../index")).resolves.not.toThrow();
    });
});
