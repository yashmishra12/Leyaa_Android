module.exports = {
  root: true,
  env: {
    node: true,
    es6: true,
  },
  extends: ["eslint:recommended"],
  parserOptions: {
    ecmaVersion: 2017,
  },
  rules: {
    "no-console": "off",
    "no-unused-vars": "off",
    "no-undef": "off",
  },
};
