import typescript from "@rollup/plugin-typescript";
import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import terser from "@rollup/plugin-terser";
import sass from "rollup-plugin-sass";
import postcssMq from "postcss-sort-media-queries";
import postcssSorting from "postcss-sorting";
import autoprefixer from "autoprefixer";
import postcss from "postcss";

const PROD = process.env.NODE_ENV === "production";

export default {
    input: "src/index.ts",
    output: {
        file: "../frontend-service/src/main/resources/static/app.js",
        format: "es",
        watch: !PROD ? {
            include: "src/**",
            clearScreen: false,
        } : undefined,
    },
    plugins: [
        resolve(),
        commonjs(),
        typescript({
            compilerOptions: {
                strict: true,
                target: "es6",
                module: "esnext",
                allowSyntheticDefaultImports: true,
                lib: ["ES2017", "DOM"],
            },
            include: ["src/**/*.ts",],
            exclude: ["node_modules", "**/*.scss"],
        }),
        sass({
            output: "../frontend-service/src/main/resources/static/app.css",
            api: "modern",
            options: {
                style: PROD ? "compressed" : "expanded",
            },
            processor: (css) =>
                postcss([
                    postcssMq(),
                    autoprefixer(),
                    postcssSorting({"properties-order": "alphabetical"}),
                ])
                    .process(css, {from: undefined})
                    .then((result) => result.css),
        }),
        PROD && terser({compress: {drop_console: true}}),
    ],
};
