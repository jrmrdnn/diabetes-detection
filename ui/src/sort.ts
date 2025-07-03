export class Sort extends HTMLElement {
    sortByField: string | null = null;
    sort: "asc" | "desc" = "asc";
    name: string | null = null;
    down = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 448 512"><path fill="currentColor" d="M201.4 374.6c12.5 12.5 32.8 12.5 45.3 0l160-160c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L224 306.7L86.6 169.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l160 160z"/></svg>`;
    up = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 448 512"><path fill="currentColor" d="M201.4 137.4c12.5-12.5 32.8-12.5 45.3 0l160 160c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L224 205.3L86.6 342.6c-12.5 12.5-32.8 12.5-45.3 0s-12.5-32.8 0-45.3l160-160z"/></svg>`;

    connectedCallback() {
        this.sortByField = this.getAttribute("sortby");
        this.name = this.getAttribute("name");

        if (!this.sortByField || !this.name) return;

        this.addListeners();
        this.render();
    }

    disconnectedCallback() {
        this.removeListeners();
    }

    addListeners() {
        this.addEventListener("click", this.handleClick);
        const params = new URLSearchParams(window.location.search);
        this.sort = params.get("sort") === "desc" ? "desc" : "asc";
    }

    removeListeners() {
        this.removeEventListener("click", this.handleClick);
    }

    handleClick = () => {
        const params = new URLSearchParams(window.location.search);
        const currentSort = params.get("sort") || "asc";
        this.sort = currentSort === "asc" ? "desc" : "asc";
        params.set("sort", this.sort);
        params.set("sortBy", this.sortByField || "lastName");
        params.set("page", "1");
        window.location.search = params.toString();
    };

    render() {
        this.innerHTML = `${this.name}<span>${this.sort === "asc" ? this.down : this.up}</span>`;
    }
}