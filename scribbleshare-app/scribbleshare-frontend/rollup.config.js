import {nodeResolve} from '@rollup/plugin-node-resolve';
import replace from '@rollup/plugin-replace';
import {terser} from "rollup-plugin-terser";

export default {
  input: 'src/scripts/main.js',
  output: {
    file: 'dist/scripts/main.js',
    format: 'iife',
  },
  plugins: [
    nodeResolve(),
    replace({
      'localhost':'scribbleshare.com'
    }),
    terser(),
  ]
};
