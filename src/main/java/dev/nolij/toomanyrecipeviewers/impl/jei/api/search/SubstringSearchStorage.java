package dev.nolij.toomanyrecipeviewers.impl.jei.api.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class SubstringSearchStorage<T> implements ISearchStorage<T>, ISearchStorageBuilder<T> {

	private final Map<String, Set<T>> valuesByKey = new LinkedHashMap<>();

	@Override
	public synchronized void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
		final Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		valuesByKey.forEach((key, values) -> {
			if (key.contains(token))
				results.addAll(values);
		});
		if (!results.isEmpty())
			resultsConsumer.accept(results);
	}

	@Override
	public synchronized void getAllElements(Consumer<Collection<T>> resultsConsumer) {
		final Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		valuesByKey.values().forEach(results::addAll);
		if (!results.isEmpty())
			resultsConsumer.accept(results);
	}

	@Override
	public synchronized void put(String key, T value) {
		valuesByKey.computeIfAbsent(key, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(value);
	}

	@Override
	public String statistics() {
		final var values = valuesByKey.values().stream().mapToInt(Collection::size).sum();
		return "%d keys, %d values".formatted(valuesByKey.size(), values);
	}

	@Override
	public ISearchStorage<T> build() {
		return this;
	}

}
