## 9.0.8

- Fix recipe lookups without an ingredient focus returning no recipes.
- Preserve existing plugin configuration files when reading fails.
- Serialize ingredient and search alias registration to prevent missing entries during parallel plugin loading.
- Isolate failures in plugin configuration callbacks so other plugins can still load.
- Forward mouse movement, scrolling, dragging, and release events to JEI recipe widgets.
- Report the ingredient actually displayed by EMI slots and fluid tanks to JEI callbacks.
- Complete JEI ghost ingredient handlers after accepting a drop.
- Restore CI artifact uploads for TMRVReforged JARs and source archives.

- Rename the fork to TMRVReforged and point project links to its own repository.

- Include fork attribution and distribution instructions in binary and source artifacts.
- Build a complete project source archive alongside each target's JAR.

## 9.0.7

- Preserve amounts, probabilities, remainders, and comparisons when copying custom ingredient stacks.
- Fix fluid aliases and return an empty lookup result for unknown item and fluid UIDs.
- Dispatch recipe widget clicks once to the correct JEI GUI listener callback.
- Save custom ingredients without codecs using their registered UIDs.
- Exclude rejected ingredients from additions during plugin registration.

## 9.0.6

- Prevent a JEI/EMI reload error when leaving a world while recipes are still registering.

## 9.0.5

- Remove the legacy JEI class compatibility shim now that Electroenergetics uses the public JEI API.

## 9.0.4

- Restore ingredient aliases for JEI plugins that use the older `TypedItemStack` package, including Electroenergetics.

## 9.0.3

- Align publishing and JAR metadata with the mod version.

## 9.0.2

- further improvements to overall system stability and other minor adjustments have been made to enhance the user experience
- Skip flowing fluid variants in recipe ingredient tags instead of displaying error placeholders; source fluids remain available.
- Fix recipes going missing at random when `registerRecipes` is dispatched in parallel: EMI's recipe list is not thread-safe, so concurrent plugin dispatch could drop recipes silently. Writes into EMI and the category/recipe lookup maps are now serialized.
