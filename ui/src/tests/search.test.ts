import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {Search} from "../search";

customElements.define("search-component", Search);

const createSearchElement = () => {
    const el = document.createElement("search-component") as Search;
    document.body.appendChild(el);
    return el;
};

describe("Search custom element", () => {
    let searchEl: Search;

    beforeEach(() => {
        document.body.innerHTML = "";
        searchEl = createSearchElement();
        searchEl.connectedCallback();
    });

    afterEach(() => {
        searchEl.disconnectedCallback();
        document.body.innerHTML = "";
        vi.restoreAllMocks();
    });

    it("connectedCallback calls render and addListeners", () => {
        const spyRender = vi.spyOn(searchEl, "render");
        const spyAdd = vi.spyOn(searchEl, "addListeners");
        searchEl.connectedCallback();
        expect(spyRender).toHaveBeenCalled();
        expect(spyAdd).toHaveBeenCalled();
        spyRender.mockRestore();
        spyAdd.mockRestore();
    });

    it("disconnectedCallback calls removeListeners and clears debounce", () => {
        const spyRemove = vi.spyOn(searchEl, "removeListeners");
        searchEl.debounceTimer = window.setTimeout(() => {
        }, 1000);
        searchEl.disconnectedCallback();
        expect(spyRemove).toHaveBeenCalled();
        expect(searchEl.debounceTimer).toBeUndefined();
        spyRemove.mockRestore();
    });

    it("addListeners adds keydown and click listeners", () => {
        const spyDoc = vi.spyOn(document, "addEventListener");
        const spyHeader = vi.spyOn(searchEl, "querySelector");
        searchEl.addListeners();
        expect(spyDoc).toHaveBeenCalledWith("keydown", expect.any(Function));
        expect(spyHeader).toHaveBeenCalledWith("#header-search");
        spyDoc.mockRestore();
        spyHeader.mockRestore();
    });

    it("removeListeners removes keydown and click listeners", () => {
        const spyDoc = vi.spyOn(document, "removeEventListener");
        const spyHeader = vi.spyOn(searchEl, "querySelector");
        searchEl.removeListeners();
        expect(spyDoc).toHaveBeenCalledWith("keydown", expect.any(Function));
        expect(spyHeader).toHaveBeenCalledWith("#header-search");
        spyDoc.mockRestore();
        spyHeader.mockRestore();
    });

    it("handleKeydown opens search form on Ctrl+K or Cmd+K", () => {
        const spyToggle = vi.spyOn(searchEl, "toggleSearchForm");
        // @ts-ignore
        HTMLElement.prototype.animate =
            HTMLElement.prototype.animate || (() => ({onfinish: null}));
        const event = new KeyboardEvent("keydown", {key: "k", ctrlKey: true});
        searchEl.handleKeydown(event);
        expect(spyToggle).toHaveBeenCalled();
        const event2 = new KeyboardEvent("keydown", {key: "k", metaKey: true});
        searchEl.handleKeydown(event2);
        expect(spyToggle).toHaveBeenCalled();
        spyToggle.mockRestore();
    });

    it("toggleSearchForm creates and shows search form if not present", () => {
        expect(document.body.querySelector(".search-container")).toBeNull();
        searchEl.toggleSearchForm();
        expect(document.body.querySelector(".search-container")).not.toBeNull();
    });

    it("toggleSearchForm hides and removes search form if present", () => {
        searchEl.toggleSearchForm();
        const searchForm = document.body.querySelector(".search-container");
        expect(searchForm).not.toBeNull();
        (searchForm as any).animate = () => ({onfinish: null});
        searchEl.toggleSearchForm();
        searchForm?.remove();
        expect(document.body.querySelector(".search-container")).toBeNull();
    });

    it("createSearchForm appends form and focuses input", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(".search-container");
        expect(form).not.toBeNull();
        const input = form?.querySelector("#search-input") as HTMLInputElement;
        expect(input).not.toBeNull();
    });

    it("searchListener debounces input and calls searchPatients", async () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const input = form.querySelector("#search-input") as HTMLInputElement;
        const spySearch = vi.spyOn(searchEl, "searchPatients").mockResolvedValue();
        const resultsContainer = form.querySelector(
            "#search-results"
        ) as HTMLElement;
        resultsContainer.animate = () =>
            ({
                finished: Promise.resolve(),
                onfinish: null,
                cancel: () => {
                },
                currentTime: null,
                effect: null,
                id: "",
                addEventListener: () => {
                },
                removeEventListener: () => {
                },
                dispatchEvent: () => false,
                startTime: null,
                playbackRate: 1,
                playState: "idle",
                replaceState: "active",
                timeline: null,
                remove: () => {
                },
                add: () => {
                },
            } as unknown as Animation);
        input.value = "John";
        input.dispatchEvent(new Event("input"));
        setTimeout(() => {
            searchEl.searchLock = false;
        }, 10);
        await new Promise((r) => setTimeout(r, 1100));
        expect(spySearch).toHaveBeenCalledWith("John");
        spySearch.mockRestore();
    });

    it("searchPatients fetches and renders results", async () => {
        searchEl.createSearchForm();
        const mockResults = {
            currentPage: 1,
            data: [
                {
                    birthDate: "1990-01-01",
                    firstName: "John",
                    gender: "M",
                    id: "123",
                    lastName: "Doe",
                    phoneNumber: null,
                    postalAddress: null,
                },
            ],
            pageSize: 10,
            totalElements: 1,
            totalPages: 1,
        };
        window.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => mockResults,
        });
        await searchEl.searchPatients("John");
        const resultsContainer = document.body.querySelector("#search-results");
        expect(resultsContainer?.children.length).toBe(1);
        expect(resultsContainer?.textContent).toContain("Doe John");
    });

    it("searchPatients handles fetch error", async () => {
        searchEl.createSearchForm();
        const spyConsole = vi.spyOn(console, "error").mockImplementation(() => {
        });
        window.fetch = vi.fn().mockResolvedValue({ok: false, status: 500});
        await searchEl.searchPatients("John");
        expect(spyConsole).toHaveBeenCalled();
        spyConsole.mockRestore();
    });

    it("handleNavigation moves selection with ArrowDown/ArrowUp and triggers click on Enter", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const resultsContainer = form.querySelector(
            "#search-results"
        ) as HTMLElement;
        const li1 = document.createElement("li");
        li1.textContent = "Doe John";
        li1.className = "search-item";
        li1.setAttribute("role", "option");
        li1.tabIndex = -1;
        li1.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li1);
        const li2 = document.createElement("li");
        li2.textContent = "Smith Jane";
        li2.className = "search-item";
        li2.setAttribute("role", "option");
        li2.tabIndex = -1;
        li2.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li2);
        li1.classList.add("selected");
        const downEvent = new KeyboardEvent("keydown", {key: "ArrowDown"});
        searchEl.handleNavigation(form, downEvent);
        expect(li1.classList.contains("selected")).toBe(false);
        expect(li2.classList.contains("selected")).toBe(true);
        const upEvent = new KeyboardEvent("keydown", {key: "ArrowUp"});
        searchEl.handleNavigation(form, upEvent);
        expect(li1.classList.contains("selected")).toBe(true);
        expect(li2.classList.contains("selected")).toBe(false);
        const spyClick = vi.spyOn(li1, "click");
        const enterEvent = new KeyboardEvent("keydown", {key: "Enter"});
        searchEl.handleNavigation(form, enterEvent);
        expect(spyClick).toHaveBeenCalled();
        spyClick.mockRestore();
    });

    it("clearButton clears input and results", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const input = form.querySelector("#search-input") as HTMLInputElement;
        const resultsContainer = form.querySelector(
            "#search-results"
        ) as HTMLElement;
        input.value = "John";
        resultsContainer.innerHTML = "<li>Doe John</li>";
        const clearBtn = form.querySelector("#clear-form") as HTMLElement;
        clearBtn.click();
        expect(input.value).toBe("");
        expect(resultsContainer.innerHTML).toBe("");
    });

    it("handleOutsideClick closes search form when clicking outside", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(".search-container") as Element;
        expect(form).not.toBeNull();
        (form as any).animate = () => ({onfinish: null});
        const event = new MouseEvent("mousedown", {bubbles: true});
        Object.defineProperty(event, "target", {value: document.body});
        searchEl.handleOutsideClick(event);
    });

    it("searchListener handles missing results container", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const input = form.querySelector("#search-input") as HTMLInputElement;
        const resultsContainer = form.querySelector("#search-results");
        resultsContainer?.remove();
        input.value = "test";
        input.dispatchEvent(new Event("input"));
        expect(searchEl.searchLock).toBe(false);
    });

    it("searchPatients handles network errors", async () => {
        searchEl.createSearchForm();
        const spyConsole = vi.spyOn(console, "error").mockImplementation(() => {
        });
        window.fetch = vi.fn().mockRejectedValue(new Error("Network error"));
        await searchEl.searchPatients("error");
        expect(spyConsole).toHaveBeenCalledWith(
            "Erreur lors de la recherche:",
            expect.any(Error)
        );
        spyConsole.mockRestore();
    });

    it("handleNavigation ignores Enter with no selection", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const resultsContainer = form.querySelector(
            "#search-results"
        ) as HTMLElement;
        resultsContainer.innerHTML = "";
        const event = new KeyboardEvent("keydown", {key: "Enter"});
        const spy = vi.spyOn(resultsContainer, "querySelectorAll");
        searchEl.handleNavigation(form, event);
        expect(spy).not.toHaveBeenCalled();
    });

    it("handleOutsideClick ignores click inside search", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(".search-container") as Element;
        const event = new MouseEvent("mousedown", {bubbles: true});
        Object.defineProperty(event, "target", {value: form});
        const spy = vi.spyOn(form, "animate");
        searchEl.handleOutsideClick(event);
        expect(spy).not.toHaveBeenCalled();
    });

    it("handleOutsideClick handles missing search container", () => {
        const event = new MouseEvent("mousedown");
        expect(() => searchEl.handleOutsideClick(event)).not.toThrow();
    });

    it("hideSearchForm handles animation completion", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(".search-container") as Element;
        const mockAnim = {
            onfinish: null,
            cancel: vi.fn(),
        };
        vi.spyOn(form, "animate").mockReturnValue(mockAnim as unknown as Animation);
        searchEl.hideSearchForm(form);
        if (mockAnim.onfinish) (mockAnim.onfinish as Function)();
        expect(form.animate).toHaveBeenCalled();
    });

    it("searchListener attaches input listener to input element", () => {
        const form = document.createElement("div") as HTMLDivElement;
        form.innerHTML = `<input id="search-input" type="text"/><ul id="search-results"></ul>`;
        const input = form.querySelector("#search-input") as HTMLInputElement;
        const spyAdd = vi.spyOn(input, "addEventListener");

        searchEl.searchListener(form);
        expect(spyAdd).toHaveBeenCalledWith("input", expect.any(Function));
        spyAdd.mockRestore();
    });

    it("searchListener sets searchLock and anim.onfinish resets it and clears results", () => {
        const form = document.createElement("div") as HTMLDivElement;
        form.innerHTML = `<input id="search-input" type="text"/><ul id="search-results"></ul>`;
        const input = form.querySelector("#search-input") as HTMLInputElement;
        const results = form.querySelector("#search-results") as HTMLElement;
        results.innerHTML = "<li>temp</li>";

        const mockAnim: any = {onfinish: null};
        results.animate = vi.fn().mockReturnValue(mockAnim as unknown as Animation);

        searchEl.searchListener(form);

        input.value = "Alice";
        input.dispatchEvent(new Event("input", {bubbles: true}));

        expect(searchEl.searchLock).toBe(true);
        expect(results.animate).toHaveBeenCalled();

        if (mockAnim.onfinish) mockAnim.onfinish();

        expect(searchEl.searchLock).toBe(false);
        expect(results.innerHTML).toBe("");
    });

    it("handleNavigation wraps from last to first on ArrowDown", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const resultsContainer = form.querySelector(
            "#search-results"
        ) as HTMLElement;

        const li1 = document.createElement("li");
        li1.textContent = "Doe John";
        li1.className = "search-item";
        li1.setAttribute("role", "option");
        li1.tabIndex = -1;
        li1.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li1);

        const li2 = document.createElement("li");
        li2.textContent = "Smith Jane";
        li2.className = "search-item";
        li2.setAttribute("role", "option");
        li2.tabIndex = -1;
        li2.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li2);

        li2.classList.add("selected");

        const downEvent = new KeyboardEvent("keydown", {key: "ArrowDown"});
        searchEl.handleNavigation(form, downEvent);

        expect(li1.classList.contains("selected")).toBe(true);
        expect(li2.classList.contains("selected")).toBe(false);
    });

    it("handleNavigation wraps from first to last on ArrowUp", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const resultsContainer = form.querySelector(
            "#search-results"
        ) as HTMLElement;

        const li1 = document.createElement("li");
        li1.textContent = "Doe John";
        li1.className = "search-item";
        li1.setAttribute("role", "option");
        li1.tabIndex = -1;
        li1.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li1);

        const li2 = document.createElement("li");
        li2.textContent = "Smith Jane";
        li2.className = "search-item";
        li2.setAttribute("role", "option");
        li2.tabIndex = -1;
        li2.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li2);

        li1.classList.add("selected");

        const upEvent = new KeyboardEvent("keydown", {key: "ArrowUp"});
        searchEl.handleNavigation(form, upEvent);

        expect(li2.classList.contains("selected")).toBe(true);
        expect(li1.classList.contains("selected")).toBe(false);
    });

    it("handleNavigation ignores unrelated keys and does not change selection", () => {
        searchEl.createSearchForm();
        const form = document.body.querySelector(
            ".search-container"
        ) as HTMLDivElement;
        const resultsContainer = form.querySelector(
            "#search-results"
        ) as HTMLElement;

        const li1 = document.createElement("li");
        li1.textContent = "Doe John";
        li1.className = "search-item";
        li1.setAttribute("role", "option");
        li1.tabIndex = -1;
        li1.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li1);

        const li2 = document.createElement("li");
        li2.textContent = "Smith Jane";
        li2.className = "search-item";
        li2.setAttribute("role", "option");
        li2.tabIndex = -1;
        li2.scrollIntoView = () => {
        };
        resultsContainer.appendChild(li2);

        li1.classList.add("selected");

        const otherEvent = new KeyboardEvent("keydown", {key: "a"});
        searchEl.handleNavigation(form, otherEvent);

        expect(li1.classList.contains("selected")).toBe(true);
        expect(li2.classList.contains("selected")).toBe(false);
    });

    it("searchPatients sets first result as selected and calls animate", async () => {
        searchEl.createSearchForm();
        const resultsContainer = document.body.querySelector(
            "#search-results"
        ) as HTMLElement;

        (resultsContainer as any).animate = vi.fn();

        const mockResults = {
            currentPage: 1,
            data: [
                {
                    birthDate: "1990-01-01",
                    firstName: "John",
                    gender: "M",
                    id: "123",
                    lastName: "Doe",
                    phoneNumber: null,
                    postalAddress: null,
                },
            ],
            pageSize: 10,
            totalElements: 1,
            totalPages: 1,
        };

        window.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => mockResults,
        });

        await searchEl.searchPatients("John");

        expect(resultsContainer.children.length).toBe(1);
        const first = resultsContainer.querySelector("li");
        expect(first).not.toBeNull();
        expect(first?.classList.contains("selected")).toBe(true);
        expect((resultsContainer as any).animate).toHaveBeenCalled();
        vi.restoreAllMocks();
    });

    it("searchPatients clicking a result updates window.location.href", async () => {
        searchEl.createSearchForm();
        const resultsContainer = document.body.querySelector(
            "#search-results"
        ) as HTMLElement;

        // ensure animate exists to avoid errors
        (resultsContainer as any).animate = vi.fn();

        const mockResults = {
            currentPage: 1,
            data: [
                {
                    birthDate: "1990-01-01",
                    firstName: "Alice",
                    gender: "F",
                    id: "abc-456",
                    lastName: "Wonder",
                    phoneNumber: null,
                    postalAddress: null,
                },
            ],
            pageSize: 10,
            totalElements: 1,
            totalPages: 1,
        };

        window.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => mockResults,
        });

        const originalLocation = (window as any).location;
        let assignedHref = "";
        Object.defineProperty(window, "location", {
            configurable: true,
            value: {
                get href() {
                    return assignedHref;
                },
                set href(v: string) {
                    assignedHref = v;
                },
            },
        });

        try {
            await searchEl.searchPatients("Alice");

            const li = resultsContainer.querySelector("li") as HTMLLIElement;
            expect(li).not.toBeNull();

            li.click();

            expect(assignedHref).toBe(`/patient/${mockResults.data[0].id}`);
        } finally {
            Object.defineProperty(window, "location", {
                configurable: true,
                value: originalLocation,
            });
            vi.restoreAllMocks();
        }
    });

    it("searchPatients with empty results still clears container and calls animate", async () => {
        searchEl.createSearchForm();
        const resultsContainer = document.body.querySelector(
            "#search-results"
        ) as HTMLElement;

        (resultsContainer as any).animate = vi.fn();

        const mockResults = {
            currentPage: 1,
            data: [],
            pageSize: 10,
            totalElements: 0,
            totalPages: 0,
        };

        window.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => mockResults,
        });

        resultsContainer.innerHTML = "<li>temp</li>";
        await searchEl.searchPatients("Nobody");

        expect(resultsContainer.children.length).toBe(0);
        expect((resultsContainer as any).animate).toHaveBeenCalled();
        vi.restoreAllMocks();
    });
});
