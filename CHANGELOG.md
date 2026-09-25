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
