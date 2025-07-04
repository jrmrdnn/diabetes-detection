export class Pagination extends HTMLElement {
    private totalElements: number | null = null;
    private currentPage: number | null = null;
    private totalPages: number | null = null;
    private pageSize: number | null = null;

    connectedCallback() {
        const totalElements = this.getAttribute("total-elements");
        this.totalElements = totalElements ? parseInt(totalElements, 10) : null;

        const currentPage = this.getAttribute("current-page");
        this.currentPage = currentPage ? parseInt(currentPage, 10) : null;

        const totalPages = this.getAttribute("total-pages");
        this.totalPages = totalPages ? parseInt(totalPages, 10) : null;

        const pageSize = this.getAttribute("page-size");
        this.pageSize = pageSize ? parseInt(pageSize, 10) : null;

        this.render();
        this.addListeners();
    }

    disconnectedCallback() {
        this.removeListeners();
    }

    private addListeners() {
        this.querySelector("#prev-button")?.addEventListener(
            "click",
            this.onPrevClick
        );
        this.querySelector("#next-button")?.addEventListener(
            "click",
            this.onNextClick
        );
        this.querySelector("#page-size")?.addEventListener(
            "change",
            this.onPageSizeChange
        );
    }

    private removeListeners() {
        this.querySelector("#prev-button")?.removeEventListener(
            "click",
            this.onPrevClick
        );
        this.querySelector("#next-button")?.removeEventListener(
            "click",
            this.onNextClick
        );
        this.querySelector("#page-size")?.removeEventListener(
            "change",
            this.onPageSizeChange
        );
    }

    private onPrevClick = () => {
        if (this.currentPage !== null) {
            const params = new URLSearchParams(window.location.search);
            params.set("page", String(this.currentPage - 1));
            window.location.search = params.toString();
        }
    };

    private onNextClick = () => {
        if (this.currentPage !== null) {
            const params = new URLSearchParams(window.location.search);
            params.set("page", String(this.currentPage + 1));
            window.location.search = params.toString();
        }
    };

    private onPageSizeChange = (event: Event) => {
        const selectElement = event.target as HTMLSelectElement;
        const newPageSize = parseInt(selectElement.value, 10);
        if (this.pageSize !== null && newPageSize !== this.pageSize) {
            const params = new URLSearchParams(window.location.search);
            params.set("size", String(newPageSize));
            window.location.search = params.toString();
        }
    };

    private render() {
        const disabledCurrentPage = this.currentPage === 1 ? "disabled" : "";

        const disabledNextPage =
            this.currentPage === this.totalPages ? "disabled" : "";

        const pageSizeFive = this.pageSize === 5 ? "selected" : "";
        const pageSizeTen = this.pageSize === 10 ? "selected" : "";
        const pageSizeTwenty = this.pageSize === 20 ? "selected" : "";
        const pageSizeFifty = this.pageSize === 50 ? "selected" : "";

        const currentPageDisplay =
            this.totalElements !== null &&
            this.pageSize !== null &&
            this.currentPage !== null
                ? Math.min(this.currentPage * this.pageSize, this.totalElements)
                : "-";

        const totalPagesDisplay =
            this.totalPages !== null && this.pageSize !== null
                ? this.totalPages * this.pageSize
                : "-";

        this.innerHTML =
            `<div class="page-size">` +
            `<span>Éléments par page</span>` +
            `<select id="page-size">` +
            `<option value="5" ${pageSizeFive}>5</option>` +
            `<option value="10" ${pageSizeTen}>10</option>` +
            `<option value="20" ${pageSizeTwenty}>20</option>` +
            `<option value="50" ${pageSizeFifty}>50</option>` +
            `</select>` +
            `</div>` +
            `<div class="pagination">` +
            `<div>${currentPageDisplay} <span>sur ${totalPagesDisplay}</span></div>` +
            `<div class="group-btn">` +
            `<button id="prev-button" ${disabledCurrentPage}><svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 512 512"><path fill="currentColor" d="M512 256a256 256 0 1 0-512 0a256 256 0 1 0 512 0M271 135c9.4-9.4 24.6-9.4 33.9 0s9.4 24.6 0 33.9l-87 87l87 87c9.4 9.4 9.4 24.6 0 33.9s-24.6 9.4-33.9 0L167 273c-9.4-9.4-9.4-24.6 0-33.9z"/></svg></button>` +
            `<button id="next-button" ${disabledNextPage}><svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 512 512"><path fill="currentColor" d="M0 256a256 256 0 1 0 512 0a256 256 0 1 0-512 0m241 121c-9.4 9.4-24.6 9.4-33.9 0s-9.4-24.6 0-33.9l87-87l-87-87c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0L345 239c9.4 9.4 9.4 24.6 0 33.9z"/></svg></button>` +
            `</div>` +
            `</div>`;
    }
}
