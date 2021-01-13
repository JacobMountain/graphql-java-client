package com.jacobmountain.graphql.client.utils

import groovy.util.logging.Slf4j

import static org.junit.Assert.assertEquals

@Slf4j
class QueryAssertion {

    static String clean(String query) {
        query.split("\n").collect { it.trim() }.join(" ").trim()
    }

    static List<String> split(String query) {
        return [
                query.find("(query|mutation|subscription)\\s*").trim(), // finds the type
                query.find("\\((\\\$\\w+:\\s+\\w+,?\\s*)+\\)"),         // finds the args
                query.find("\\{.*")                                     // finds the selections
        ]
    }

    static void assertArgs(String expected = "", String actual) {
        actual = actual ?: "()"
        expected = expected ?: "()"
        assertEquals("Args are incorrect", "(", actual.substring(0, 1))
        assertEquals("Args are incorrect", ")", actual.substring(actual.length() - 1))
        assertEquals(
                "Args are incorrect",
                expected.substring(1, expected.length() - 1).split(", ") as Set,
                actual.substring(1, expected.length() - 1).split(", ") as Set
        )
    }

    static class Fragment {

        String name

        String on

        String selection

        Fragment(String name, String on, String selection) {
            this.name = name
            this.on = on
            this.selection = selection
        }

        @Override
        String toString() {
            "$name on $on $selection"
        }

        String inline() {
            "... on $on $selection"
        }

        String regex() {
            def spaces = "\\s+"
            "$name${spaces}on$spaces$on$spaces${selection.replaceAll(spaces, "\\\\s+")}"
        }

        String spreadRegex() {
            "...\\s*$name"
        }
    }

    static Map<String, Fragment> collectFragments(String input) {
        input.split("fragment")
                .tail()
                .collect {
                    def on = it.split("on")
                    new Fragment(
                            on[0].trim(),
                            on[1].find("\\w+"),
                            it.find("\\{.*")
                    )
                }
                .collectEntries() { [(it.name): it] }
    }

    static String expandFragments(String input) {
        def fragments = collectFragments(input)
        def output = input.split("fragment")[0].trim()
        fragments.values()
                .forEach { fragment -> output = output.replaceAll(fragment.spreadRegex(), fragment.inline()) }
        output
    }

    static void assertQueriesAreEqual(String expected, String result) {
        log.info("Asserting equal: ")
        log.info("\texpected: {}", expected)
        log.info("\tactual:   {}", result)
        expected = expandFragments(clean(expected))
        result = expandFragments(clean(result))
        log.info("Cleaned: ")
        log.info("\texpected: {}", expected)
        log.info("\tactual:   {}", result)
        def a = split(expected)
        def b = split(result)

        assertEquals("Type is incorrect", a[0], b[0])
        assertArgs(a[1], b[1])
        assertEquals("Fields are incorrect", a[2], b[2])
    }

}
