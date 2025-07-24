import { User } from "./user";
import { Sort } from "./sort";
import { Search } from "./search";
import { Textarea } from "./textarea";
import { Timeline } from "./timeline";
import { Pagination } from "./pagination";

import "./scss/index.scss";

customElements.define("user-component", User);
customElements.define("sort-component", Sort);
customElements.define("search-component", Search);
customElements.define("textarea-component", Textarea);
customElements.define("timeline-component", Timeline);
customElements.define("pagination-component", Pagination);
