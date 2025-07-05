interface PatientsResults {
    currentPage: number;
    data: Array<{
        birthDate: string;
        firstName: string;
        gender: "M" | "F";
        id: string;
        lastName: string;
        phoneNumber: string | null;
        postalAddress: string | null;
    }>;
    pageSize: number;
    totalElements: number;
    totalPages: number;
}

export class Search extends HTMLElement {
    inputListener?: (event: Event) => void;
    debounceTimer?: number;
    searchLock = false;
    boundHandleKeydown: (event: KeyboardEvent) => void;
    boundHandleOutsideClick: (event: MouseEvent) => void;
    boundToggleSearchForm: () => void;

    constructor() {
        super();
        this.boundHandleKeydown = this.handleKeydown.bind(this);
        this.boundToggleSearchForm = this.toggleSearchForm.bind(this);
        this.boundHandleOutsideClick = this.handleOutsideClick.bind(this);
    }

    connectedCallback(): void {
        this.render();
        this.addListeners();
    }

    disconnectedCallback(): void {
        this.removeListeners();
        if (this.debounceTimer) {
            window.clearTimeout(this.debounceTimer);
            this.debounceTimer = undefined;
        }
    }

    addListeners(): void {
        document.addEventListener("keydown", this.boundHandleKeydown);
        this.querySelector("#header-search")?.addEventListener("click", this.boundToggleSearchForm);
    }

    removeListeners(): void {
        document.removeEventListener("keydown", this.boundHandleKeydown);
        this.querySelector("#header-search")?.removeEventListener("click", this.boundToggleSearchForm);
    }

    handleKeydown(event: KeyboardEvent): void {
        if ((event.ctrlKey || event.metaKey) && event.key === "k") {
            event.preventDefault();
            this.toggleSearchForm();
        }
    }

    toggleSearchForm(): void {
        const search = document.body.querySelector(".search-container");
        if (search) {
            this.hideSearchForm(search);
            document.removeEventListener("mousedown", this.boundHandleOutsideClick);
        } else {
            this.createSearchForm();
            document.addEventListener("mousedown", this.boundHandleOutsideClick);
        }
    }

    hideSearchForm(search: Element): void {
        const searchInput = search.querySelector("#search-input");
        if (searchInput && this.inputListener) {
            searchInput.removeEventListener("input", this.inputListener);
        }
        const anim = search.animate([{opacity: 1, transform: "translateY(0)"}, {
            opacity: 0,
            transform: "translateY(-200px)"
        },], {
            duration: 300, easing: "ease-in-out", fill: "forwards",
        });
        anim.onfinish = () => {
            search.remove();
            document.removeEventListener("mousedown", this.boundHandleOutsideClick);
        };
    }

    createSearchForm(): void {
        const form = document.createElement("div");
        form.className = "search-container";
        form.innerHTML =
            `<div class="search-form">` +
            `<input id="search-input" type="text" placeholder="Rechercher un patient ..." class="search-input" aria-label="Recherche patient" role="searchbox">` +
            `<span id="clear-form" title="Effacer la recherche">` +
            `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 384 512"><path fill="currentColor" d="M342.6 150.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192 210.7L86.6 105.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L146.7 256L41.4 361.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192 301.3l105.4 105.3c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.3 256z"/></svg>` +
            `</span>` +
            `</div>` +
            `<ul id="search-results" class="search-results" role="listbox" aria-label="Résultats de recherche"></ul>`;

        document.body.appendChild(form);
        this.clearButton(form);
        this.searchListener(form);

        const searchInput = form.querySelector("#search-input") as HTMLInputElement;
        searchInput.addEventListener("keydown", this.handleNavigation.bind(this, form)!);

        requestAnimationFrame(() => {
            form.classList.add("show");
            searchInput.focus();
        });
    }

    searchListener(form: HTMLDivElement) {
        const searchInput = form.querySelector("#search-input") as HTMLInputElement;
        if (searchInput) {
            this.inputListener = (event: Event) => {
                const target = event.target as HTMLInputElement;
                const value = target.value.trim();

                const resultsContainer = form.querySelector("#search-results") as HTMLElement;

                if (this.searchLock) return;

                if (resultsContainer) {
                    this.searchLock = true;
                    const anim = resultsContainer.animate([{opacity: 1, transform: "translateY(0)"}, {
                        opacity: 0,
                        transform: "translateY(-20px)"
                    },], {
                        duration: 300, easing: "ease-in-out", fill: "forwards",
                    });
                    anim.onfinish = () => {
                        this.searchLock = false;
                        resultsContainer.innerHTML = "";
                    };
                }

                if (this.debounceTimer) window.clearTimeout(this.debounceTimer);
                this.debounceTimer = window.setTimeout(() => !this.searchLock && value.length > 0 && this.searchPatients(value), 1_000);
            };

            searchInput.addEventListener("input", this.inputListener);
        }
    }

    // @ts-ignore
    async searchPatients(value: string): Promise<void> {
        try {
            const response = await fetch(`/api/patients/search?q=${encodeURIComponent(value)}`);

            if (!response.ok) throw new Error(`Erreur HTTP: ${response.status}`);

            const results: PatientsResults = await response.json();

            const resultsContainer = document.body.querySelector("#search-results") as HTMLElement;
            if (resultsContainer) {
                resultsContainer.innerHTML = "";
                results.data.forEach((patient) => {
                    const item = document.createElement("li");
                    item.className = "search-item";
                    item.textContent = `${patient.lastName} ${patient.firstName}`;
                    item.setAttribute("role", "option");
                    item.tabIndex = -1;
                    item.addEventListener("click", () => {
                        window.location.href = `/patient/${patient.id}`;
                    });
                    resultsContainer.appendChild(item);
                });

                const first = resultsContainer.querySelector("li");
                if (first) first.classList.add("selected");
                resultsContainer.animate([{opacity: 0, transform: "translateY(-20px)"}, {
                    opacity: 1,
                    transform: "translateY(0)"
                },], {
                    duration: 300, easing: "ease-in-out", fill: "forwards",
                });
            }
        } catch (error) {
            console.error("Erreur lors de la recherche:", error);
        }
    }

    handleNavigation(form: HTMLDivElement, event: KeyboardEvent): void {
        const results = form.querySelectorAll<HTMLLIElement>("#search-results li");
        if (!results.length) return;

        // @ts-ignore
        let idx = Array.from(results).findIndex((li: {
            classList: { contains: (arg0: string) => any; };
        }) => li.classList.contains("selected"));

        switch (event.key) {
            case "ArrowDown":
                event.preventDefault();
                idx = idx < results.length - 1 ? idx + 1 : 0;
                break;
            case "ArrowUp":
                event.preventDefault();
                idx = idx > 0 ? idx - 1 : results.length - 1;
                break;
            case "Enter":
                if (idx >= 0) {
                    event.preventDefault();
                    results[idx].click();
                }
                return;
            default:
                return;
        }

        results.forEach((li) => li.classList.remove("selected"));
        const current = results[idx];
        current.classList.add("selected");
        current.scrollIntoView({block: "nearest"});
    }

    clearButton(form: HTMLDivElement) {
        const clearButton = form.querySelector("#clear-form");
        const searchInput = form.querySelector("#search-input") as HTMLInputElement;

        if (clearButton && searchInput) {
            clearButton.addEventListener("click", () => {
                searchInput.value = "";
                searchInput.focus();

                const resultsContainer = form.querySelector("#search-results") as HTMLElement;
                if (resultsContainer) resultsContainer.innerHTML = "";
            });
        }
    }

    handleOutsideClick(event: MouseEvent): void {
        const search = document.body.querySelector(".search-container");
        const header = this.querySelector("#header-search");
        if (search && !search.contains(event.target as Node) && header && !header.contains(event.target as Node)) {
            this.hideSearchForm(search);
        }
    }

    render(): void {
        this.innerHTML =
            `<div id="header-search" class="header-search" tabindex="0" aria-label="Ouvrir la recherche">` +
            `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 512 512"><path fill="currentColor" d="M416 208c0 45.9-14.9 88.3-40 122.7l126.6 126.7c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L330.7 376c-34.4 25.2-76.8 40-122.7 40C93.1 416 0 322.9 0 208S93.1 0 208 0s208 93.1 208 208M208 352a144 144 0 1 0 0-288a144 144 0 1 0 0 288"/></svg>` +
            `</div>`;
    }
}
