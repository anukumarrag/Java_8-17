package com.training.bonus.sequencedcollections;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.TreeMap;

/**
 * =============================================================================
 * BONUS – SEQUENCED COLLECTIONS (JEP 431, Java 21 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * Before Java 21, the Collection framework had a gap: there was no common
 * supertype for collections that have a well-defined ENCOUNTER ORDER (first,
 * last, reverse iteration).
 *
 * Getting the first or last element required:
 *   • List:        list.get(0)  /  list.get(list.size()-1)
 *   • Deque:       deque.peekFirst() / deque.peekLast()
 *   • SortedSet:   set.first()  / set.last()
 *   • LinkedHashSet: iterate to find last – O(n)!
 *
 * Java 21 introduced THREE new interfaces:
 *
 *   SequencedCollection<E>       – any ordered collection
 *     getFirst() / getLast()     – access first/last element
 *     addFirst() / addLast()     – insert at head/tail
 *     removeFirst() / removeLast() – remove from head/tail
 *     reversed()                 – view in reverse order
 *
 *   SequencedSet<E> extends SequencedCollection<E> + Set<E>
 *     (same methods, no duplicate elements)
 *
 *   SequencedMap<K,V>
 *     firstEntry() / lastEntry()     – Map.Entry at head/tail
 *     putFirst() / putLast()         – insert at head/tail
 *     sequencedKeySet() / sequencedValues() / sequencedEntrySet()
 *     reversed()                     – reversed view of the map
 *
 * EXISTING TYPES THAT NOW IMPLEMENT THESE
 * ----------------------------------------
 *   List            → SequencedCollection
 *   Deque           → SequencedCollection
 *   LinkedHashSet   → SequencedSet
 *   SortedSet       → SequencedSet
 *   LinkedHashMap   → SequencedMap
 *   SortedMap       → SequencedMap
 *
 * REQUIRES: Java 21
 */
public class SequencedCollectionsExamples {

    public record Trade(String id, String symbol, double notional) {}

    // =========================================================================
    // BEFORE – Verbose first/last access (Java 17)
    // =========================================================================

    public Trade getFirstTrade_Before(List<Trade> trades) {
        if (trades.isEmpty()) throw new java.util.NoSuchElementException();
        return trades.get(0);
    }

    public Trade getLastTrade_Before(List<Trade> trades) {
        if (trades.isEmpty()) throw new java.util.NoSuchElementException();
        return trades.get(trades.size() - 1);
    }

    // =========================================================================
    // AFTER – SequencedCollection (Java 21)
    // =========================================================================

    /** getFirst() / getLast() – no more get(0) / get(size-1). */
    public Trade getFirstTrade(List<Trade> trades) {
        return trades.getFirst();   // SequencedCollection method
    }

    public Trade getLastTrade(List<Trade> trades) {
        return trades.getLast();    // SequencedCollection method
    }

    // ---- addFirst / addLast -------------------------------------------------

    /** Insert a trade at the BEGINNING of the execution queue. */
    public List<Trade> prioritiseTrade(List<Trade> queue, Trade priority) {
        var mutable = new ArrayList<>(queue);
        mutable.addFirst(priority);    // O(1) for LinkedList / ArrayList shifts
        return mutable;
    }

    public List<Trade> appendTrade(List<Trade> queue, Trade trade) {
        var mutable = new ArrayList<>(queue);
        mutable.addLast(trade);
        return mutable;
    }

    // ---- removeFirst / removeLast -------------------------------------------

    /** Consume the next trade from the head of the processing queue. */
    public Trade consumeNext(List<Trade> queue) {
        return queue.removeFirst();
    }

    // ---- reversed() ---------------------------------------------------------

    /**
     * reversed(): returns a REVERSED VIEW of the collection.
     * Changes to the original are reflected in the reversed view.
     * No copy is made.
     */
    public List<Trade> reverseOrder(List<Trade> trades) {
        return trades.reversed();   // O(1) – just a view
    }

    /** Iterate newest-first (most recently added = last). */
    public Trade mostRecentTrade(List<Trade> trades) {
        return trades.reversed().getFirst();   // same as getLast()
    }

    // ---- SequencedSet (LinkedHashSet) ---------------------------------------

    /**
     * LinkedHashSet now exposes getFirst() / getLast() via SequencedSet.
     * Before Java 21: you had to iterate the entire set to find the last element.
     */
    public String firstAddedSymbol(LinkedHashSet<String> symbols) {
        return symbols.getFirst();
    }

    public String lastAddedSymbol(LinkedHashSet<String> symbols) {
        return symbols.getLast();   // O(1) now, not O(n)!
    }

    public SequencedSet<String> reversedSymbols(LinkedHashSet<String> symbols) {
        return symbols.reversed();
    }

    // ---- SequencedMap (TreeMap / LinkedHashMap) ------------------------------

    /** Get first/last key-value pair from a sorted map. */
    public java.util.Map.Entry<String, Double> cheapestAsset(TreeMap<String, Double> priceMap) {
        return priceMap.firstEntry();   // smallest key (e.g., alphabetically)
    }

    public java.util.Map.Entry<String, Double> mostExpensiveAsset(TreeMap<String, Double> priceMap) {
        return priceMap.lastEntry();    // largest key
    }

    /**
     * reversed() on a SequencedMap: iterate from highest to lowest price.
     */
    public SequencedMap<String, Double> highToLowPrices(TreeMap<String, Double> priceMap) {
        return priceMap.reversed();
    }

    // =========================================================================
    // Writing to a SequencedCollection parameter (polymorphic)
    // =========================================================================

    /**
     * Accept ANY sequenced collection and process first and last.
     * Works with ArrayList, LinkedList, ArrayDeque, etc.
     */
    public String summarise(SequencedCollection<String> items) {
        if (items.isEmpty()) return "EMPTY";
        return "first=" + items.getFirst() + " last=" + items.getLast()
                + " count=" + items.size();
    }

    // demo main
    public static void main(String[] args) {
        SequencedCollectionsExamples ex = new SequencedCollectionsExamples();

        List<Trade> trades = new ArrayList<>(List.of(
                new Trade("T1", "AAPL", 100_000),
                new Trade("T2", "MSFT", 200_000),
                new Trade("T3", "GOOG", 300_000)
        ));

        System.out.println("First     : " + ex.getFirstTrade(trades).id());
        System.out.println("Last      : " + ex.getLastTrade(trades).id());

        var priority = new Trade("T0", "TSLA", 50_000);
        var prioritised = ex.prioritiseTrade(trades, priority);
        System.out.println("Prioritised first: " + prioritised.getFirst().id());

        System.out.println("Reversed  : " +
                ex.reverseOrder(trades).stream().map(Trade::id).toList());

        var symbols = new LinkedHashSet<>(List.of("AAPL", "MSFT", "GOOG", "TSLA"));
        System.out.println("First sym : " + ex.firstAddedSymbol(symbols));
        System.out.println("Last sym  : " + ex.lastAddedSymbol(symbols));

        var priceMap = new TreeMap<>(java.util.Map.of(
                "AAPL", 182.50, "GOOG", 172.30, "MSFT", 415.00));
        System.out.println("Cheapest  : " + ex.cheapestAsset(priceMap));
        System.out.println("Priciest  : " + ex.mostExpensiveAsset(priceMap));

        System.out.println("Summarise : " + ex.summarise(List.of("alpha", "beta", "gamma")));
    }
}
