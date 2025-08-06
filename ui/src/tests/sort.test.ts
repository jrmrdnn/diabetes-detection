import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {Sort} from "../sort";

customElements.define("sort-component", Sort);

const createSortElement = (attrs: { sortby?: string; name?: string } = {}) => {
    const el = document.createElement("sort-component") as Sort;
    if (attrs.sortby) el.setAttribute("sortby", attrs.sortby);
    if (attrs.name) el.setAttribute("name", attrs.name);
    document.body.appendChild(el);
    return el;
};

describe("Sort custom element", () => {
    let sortEl: Sort;

    beforeEach(() => {
        window.history.replaceState({}, "", "/");
        sortEl = createSortElement({sortby: "lastName", name: "Nom"});
        sortEl.connectedCallback();
    });

    afterEach(() => {
        sortEl.disconnectedCallback();
        document.body.innerHTML = "";
    });

    it("connectedCallback sets attributes and calls addListeners/render", () => {
        //expect(sortEl.sortby).toBe("lastName");
        expect(sortEl.name).toBe("Nom");
        expect(sortEl.innerHTML).toContain("Nom");
        expect(sortEl.innerHTML).toContain("svg");
    });

    it("connectedCallback does nothing if attributes missing", () => {
        const el = createSortElement();
        const spyAdd = vi.spyOn(el, "addListeners");
        el.connectedCallback();
        expect(spyAdd).not.toHaveBeenCalled();
        spyAdd.mockRestore();
        el.remove();
    });

    it("disconnectedCallback calls removeListeners", () => {
        const spyRemove = vi.spyOn(sortEl, "removeListeners");
        sortEl.disconnectedCallback();
        expect(spyRemove).toHaveBeenCalled();
        spyRemove.mockRestore();
    });

    it("addListeners adds click event and sets sort from URL", () => {
        const spyAdd = vi.spyOn(sortEl, "addEventListener");
        sortEl.addListeners();
        expect(spyAdd).toHaveBeenCalledWith("click", sortEl.handleClick);
        spyAdd.mockRestore();
        window.history.replaceState({}, "", "?sort=desc");
        sortEl.addListeners();
        expect(sortEl.sort).toBe("asc");
    });

    it("removeListeners removes click event", () => {
        const spyRemove = vi.spyOn(sortEl, "removeEventListener");
        sortEl.removeListeners();
        expect(spyRemove).toHaveBeenCalledWith("click", sortEl.handleClick);
        spyRemove.mockRestore();
    });

    it("handleClick toggles sort direction and updates URL params", () => {
        expect(sortEl.sort).toBe("asc");
        sortEl.handleClick();
        expect(sortEl.sort).toBe("desc");
        expect(window.location.search).toContain("sort=desc");
        expect(window.location.search).toContain("sortBy=lastName");
        expect(window.location.search).toContain("page=1");

        sortEl.handleClick();
        expect(sortEl.sort).toBe("asc");
        expect(window.location.search).toContain("sort=asc");
    });

    it("render displays correct name and SVG based on sort", () => {
        sortEl.sort = "asc";
        sortEl.render();
        expect(sortEl.innerHTML).toContain("Nom");
        expect(sortEl.innerHTML).toContain("svg");
        expect(sortEl.innerHTML).toContain("374.6");
        sortEl.sort = "desc";
        sortEl.render();
        expect(sortEl.innerHTML).toContain("137.4");
    });

    it("sortby defaults to lastName if missing", () => {
        const el = createSortElement({name: "Nom"});
        el.connectedCallback();
        el.handleClick();
        expect(window.location.search).toContain("sortBy=lastName");
        el.remove();
    });

    it("multiple clicks toggle sort direction each time", () => {
        sortEl.handleClick();
        expect(sortEl.sort).toBe("asc");
        sortEl.handleClick();
        expect(sortEl.sort).toBe("desc");
        sortEl.handleClick();
        expect(sortEl.sort).toBe("asc");
    });
});
