

# Replay search in BWHF Agent #

The Replay search tab in BWHF Agent let's you search replays by various conditions. If you don't touch any filters, the search returns all the input replays. If you specify some conditions, they will be in logical _AND_ connection.

## Replay filter fields ##
There are different types of filter fields. When there are some predefined values, you will see them listed with checkboxes. You can check any that you're interested in. They will be in logical _OR_ connection: if any of the selected values apply to a replay, the replay will be included in the results.

Values entered to text fields are case insensitive and will be trimmed: redundant spaces will be removed from the beginning and from the end of the entered values. Text fields have an _Exact match_ option: if this is selected, the entered value must match exactly the replay property they apply to, else the entered value may be a substring. If exact match is not used, you can enter either a single value or a comma separated value list. Text fields also have a _Regexp_ option: if this is checked, the entered value will be interpreted as a _regular expression_. With regular expression you can define _patterns_ which can match lot of values. You can see some examples below on this page.

There are some fields which can have values from a wide range. In these cases the filer is an interval filter: you can define the minimal and the maximal accepted values. You are allowed to only set one of them, and then the other end of the interval will not be limited. When the interval is limited to some valid values, you will see a combo box with the valid values, and you can select the one you wish to set as the limit.

## Search execution buttons ##
Search can be executed by specifying the set of replays to search. The currently running search can be terminated at any time with the **Stop current search** button.

There are several buttons to define the source replays of search:
  * **Select files** button: you can select multiple replay files to search.
  * **Select folders to search recursively** button: you can select folders of replays to search recursively.
  * **Repeat search** button: the search will be repeated on the same replays as the last search but with the newly specified or modified filters of course.
  * **Search in previous results (narrows previous result)** button: the search will be executed on the result of the last search. You can filter previous results with this button.

## Search results ##
The number of searched replays and the number of replays matching the filters are displayed above the result table. The result table lists every replay matching the filters. You can select any of the replays, and you can perform various operations on them with the buttons being on the right of the table. If you double click on a replay, it will be shown on the Charts tab.

## Regular expression examples ##
Regular expression is a powerful tool to simplify complex searches. If you want learn more about regular expression, you can google it or check the following links: [Regular expressions on wikipedia](http://en.wikipedia.org/wiki/Regular_expression), [Java regexp syntax](http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html#sum)

Some examples for regular expressions:
  * If you don't know or don't remember exactly a character in a player's name (maybe because they look alike in Starcraft: 'I', 'l', '|'). Let's search the word _window_ where the 2nd character can be anything. The dot ('.') in regexp matches any character: **`w.ndow`**, or if you want to allow only some characters: **`w[il|]ndow`**
  * Words that start with _dak_ and end with _ing_. The `.*` matches any string: **`dak.*ing`**
  * Words that start with any non-word character and ends with some number: **`\D+\d+`**
  * Words that has at least 3 x in it in a sequence: **`x{3,}+`**