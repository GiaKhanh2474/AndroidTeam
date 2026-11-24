// Temporary workaround for https://github.com/auth0/node-jwks-rsa/issues/507.
// Remove the override and this script after adopting a tested upstream fix.
const fs = require("node:fs");
const path = require("node:path");
const { createHash } = require("node:crypto");

const patches = [
  {
    file: "src/utils.js",
    before: "c535773fd798202296e846e7c057f96f631148686194ff2990f0729fa4c1b7af",
    after: "813561324cf80ec19981c32d9c0041d9a458c4c0cb09b9ac943e36ba6a435b23",
    transform: source => source.replace(
      "async function retrieveSigningKeys(jwks) {",
      "async function retrieveSigningKeys(jwks) {\n  const jose = await loadJose();",
    ),
  },
  {
    file: "src/integrations/passport.js",
    before: "03557fac70296dda6d19872b1702d2b8f1d4c50d22d3fbcf1fb65f9afa7ddcbb",
    after: "de1ddf292801643ecbddfa77a6b793f9c4bf82ab42d6b4d6dc28ada7a0d56597",
    transform: source => source.replace(
      "return function secretProvider(req, rawJwtToken, cb) {",
      "return async function secretProvider(req, rawJwtToken, cb) {",
    ).replace(
      "    try {\n      decoded = {",
      "    try {\n      const jose = await loadJose();\n      decoded = {",
    ),
  },
];

const hash = source => createHash("sha256").update(source).digest("hex");

function patchJwksRsa(directory = path.dirname(require.resolve("jwks-rsa/package.json"))) {
  const { version } = JSON.parse(fs.readFileSync(path.join(directory, "package.json"), "utf8"));
  if (version !== "4.1.0") throw new Error(`Review the jose workaround for jwks-rsa ${version}`);

  // Validate every file before writing, and allow repeated npm install runs.
  const changes = patches.map(patch => {
    const file = path.join(directory, patch.file);
    const source = fs.readFileSync(file, "utf8").replace(/\r\n/g, "\n");
    if (hash(source) === patch.after) return null;
    if (hash(source) !== patch.before) throw new Error(`Unexpected jwks-rsa source: ${patch.file}`);
    const output = patch.transform(source.replace(
      "const jose = require('jose');",
      "let josePromise;\nconst loadJose = () => (josePromise ||= import('jose'));",
    ));
    if (hash(output) !== patch.after) throw new Error(`Invalid jose patch: ${patch.file}`);
    return { file, output };
  }).filter(Boolean);

  for (const { file, output } of changes) fs.writeFileSync(file, output);
  return changes.length;
}

if (require.main === module) {
  console.log(`[compat] jwks-rsa 4.1.0: ${patchJwksRsa()} file(s) updated for async jose loading`);
}
module.exports = { patchJwksRsa };
