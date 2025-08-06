import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {Pagination} from "../pagination";

customElements.define("app-pagination", Pagination);

function createPaginationElement(
    attrs: Partial<{
        currentPage: string;
        totalPages: string;
        pageSize: string;
    }> = {}
) {
    const el = document.createElement("app-pagination") as Pagination;
    if (attrs.currentPage) el.setAttribute("current-page", attrs.currentPage);
    if (attrs.totalPages) el.setAttribute("total-pages", attrs.totalPages);
    if (attrs.pageSize) el.setAttribute("page-size", attrs.pageSize);
    document.body.appendChild(el);
    return el;
}

describe("Pagination custom element", () => {
    let paginationEl: Pagination;

    beforeEach(() => {
        paginationEl = createPaginationElement({
            currentPage: "2",
            totalPages: "5",
            pageSize: "10",
        });
        paginationEl.connectedCallback();
    });

    afterEach(() => {
        paginationEl.disconnectedCallback();
        document.body.innerHTML = "";
        vi.restoreAllMocks();
    });

    it("renders correct innerHTML and selected page size", () => {
        expect(paginationEl.innerHTML).toContain('id="page-size"');
        expect(paginationEl.innerHTML).toMatch(
            /<option value="10" selected(="")?>/
        );
        expect(paginationEl.innerHTML).toContain('<button id="prev-button">');
        expect(paginationEl.innerHTML).toContain('<button id="next-button">');
    });

    it("disables prev button on first page", () => {
        paginationEl.setAttribute("current-page", "1");
        paginationEl.connectedCallback();
        expect(paginationEl.innerHTML).toMatch(
            /<button id="prev-button" disabled(="")?>/
        );
    });

    it("disables next button on last page", () => {
        paginationEl.setAttribute("current-page", "5");
        paginationEl.connectedCallback();
        expect(paginationEl.innerHTML).toMatch(
            /<button id="next-button" disabled(="")?>/
        );
    });

    it("sets correct selected option for page size", () => {
        paginationEl.setAttribute("page-size", "20");
        paginationEl.connectedCallback();
        expect(paginationEl.innerHTML).toMatch(
            /<option value="20" selected(="")?>/
        );
    });

    it("prev button click updates window.location.search", () => {
        const spy = vi.spyOn(window, "location", "get").mockReturnValue({
            set search(val: string) {
                this._search = val;
            },
            get search() {
                return this._search || "?page=2";
            },
            _search: "?page=2",
        } as any);

        const setSpy = vi.spyOn(window.location, "search", "set");
        paginationEl
            .querySelector("#prev-button")
            ?.dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(setSpy).toHaveBeenCalledWith("page=1");
        setSpy.mockRestore();
        spy.mockRestore();
    });

    it("next button click updates window.location.search", () => {
        const spy = vi.spyOn(window, "location", "get").mockReturnValue({
            set search(val: string) {
                this._search = val;
            },
            get search() {
                return this._search || "?page=2";
            },
            _search: "?page=2",
        } as any);

        const setSpy = vi.spyOn(window.location, "search", "set");
        paginationEl
            .querySelector("#next-button")
            ?.dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(setSpy).toHaveBeenCalledWith("page=3");
        setSpy.mockRestore();
        spy.mockRestore();
    });

    it("page size change updates window.location.search", () => {
        const spy = vi.spyOn(window, "location", "get").mockReturnValue({
            set search(val: string) {
                this._search = val;
            },
            get search() {
                return this._search || "?size=10";
            },
            _search: "?size=10",
        } as any);

        const setSpy = vi.spyOn(window.location, "search", "set");
        const select = paginationEl.querySelector(
            "#page-size"
        ) as HTMLSelectElement;
        select.value = "20";
        select.dispatchEvent(new Event("change", {bubbles: true}));
        expect(setSpy).toHaveBeenCalledWith("size=20");
        setSpy.mockRestore();
        spy.mockRestore();
    });

    it("does not update window.location.search if page size unchanged", () => {
        const spy = vi.spyOn(window, "location", "get").mockReturnValue({
            set search(val: string) {
                this._search = val;
            },
            get search() {
                return this._search || "?size=10";
            },
            _search: "?size=10",
        } as any);

        const setSpy = vi.spyOn(window.location, "search", "set");
        const select = paginationEl.querySelector(
            "#page-size"
        ) as HTMLSelectElement;
        select.value = "10";
        select.dispatchEvent(new Event("change", {bubbles: true}));
        expect(setSpy).not.toHaveBeenCalled();
        setSpy.mockRestore();
        spy.mockRestore();
    });

    it("removes listeners on disconnectedCallback", () => {
        const prevBtn = paginationEl.querySelector("#prev-button")!;
        const nextBtn = paginationEl.querySelector("#next-button")!;
        const pageSize = paginationEl.querySelector("#page-size")!;

        const prevSpy = vi.spyOn(prevBtn, "removeEventListener");
        const nextSpy = vi.spyOn(nextBtn, "removeEventListener");
        const sizeSpy = vi.spyOn(pageSize, "removeEventListener");

        paginationEl.disconnectedCallback();

        expect(prevSpy).toHaveBeenCalledWith("click", expect.any(Function));
        expect(nextSpy).toHaveBeenCalledWith("click", expect.any(Function));
        expect(sizeSpy).toHaveBeenCalledWith("change", expect.any(Function));

        prevSpy.mockRestore();
        nextSpy.mockRestore();
        sizeSpy.mockRestore();
    });

    it("handles null attributes gracefully", () => {
        const el = createPaginationElement();
        el.connectedCallback();
        expect(el.innerHTML).toContain('<option value="5">');
        expect(el.innerHTML).toContain('<option value="10">');
        expect(el.innerHTML).toContain('<option value="20">');
        expect(el.innerHTML).toContain('<option value="50">');
    });

    it("select element has correct selected option for page size", () => {
        paginationEl.setAttribute("page-size", "50");
        paginationEl.connectedCallback();
        const select = paginationEl.querySelector(
            "#page-size"
        ) as HTMLSelectElement;
        expect(select.value).toBe("50");
        expect(
            (select.querySelector('option[value="50"]') as HTMLOptionElement)
                ?.selected
        ).toBe(true);
    });

    it("renders correct current and total display when totalElements less than page*pageSize", () => {
        paginationEl.setAttribute("current-page", "2");
        paginationEl.setAttribute("total-pages", "2");
        paginationEl.setAttribute("page-size", "10");
        paginationEl.setAttribute("total-elements", "15");
        paginationEl.connectedCallback();
        expect(paginationEl.innerHTML).toContain("15 <span>sur 20</span>");
    });

    it("does not change window.location.search when currentPage is null and prev/next clicked", () => {
        const el = createPaginationElement({pageSize: "10", totalPages: "5"});
        el.connectedCallback();

        const spy = vi.spyOn(window, "location", "get").mockReturnValue({
            set search(val: string) {
                this._search = val;
            },
            get search() {
                return this._search || "?page=1";
            },
            _search: "?page=1",
        } as any);

        const setSpy = vi.spyOn(window.location, "search", "set");

        el.querySelector("#prev-button")?.dispatchEvent(
            new MouseEvent("click", {bubbles: true})
        );
        el.querySelector("#next-button")?.dispatchEvent(
            new MouseEvent("click", {bubbles: true})
        );

        expect(setSpy).not.toHaveBeenCalled();

        setSpy.mockRestore();
        spy.mockRestore();
    });

    it("calls addListeners during connectedCallback", () => {
        const addSpy = vi.spyOn(Pagination.prototype as any, "addListeners");
        const el = createPaginationElement({
            currentPage: "1",
            totalPages: "3",
            pageSize: "10",
        });
        el.connectedCallback();
        expect(addSpy).toHaveBeenCalled();
        addSpy.mockRestore();
    });

    it("calls removeListeners during disconnectedCallback", () => {
        const removeSpy = vi.spyOn(Pagination.prototype as any, "removeListeners");
        const el = createPaginationElement({
            currentPage: "1",
            totalPages: "3",
            pageSize: "10",
        });
        el.connectedCallback();
        el.disconnectedCallback();
        expect(removeSpy).toHaveBeenCalled();
        removeSpy.mockRestore();
    });
});
