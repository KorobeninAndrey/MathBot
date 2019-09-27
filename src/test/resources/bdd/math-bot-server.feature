@math-bot
  Feature: 1
    Scenario Outline: check simple math calc
      When input is "<input>"
      Then response is "<response>"
      Examples:
        | input    | response         |

        |hello     |  world           |
        |author    | Andrey Korobenin |
        |1+0       | 1.0              |
        |2 + 3     | 5.0              |
        |2 - 1     | 1.0              |
        | 2 - 3    | -1.0             |
        | 2 - 3 - 1| -2.0             |
        |2 + (3-4) |1.0               |
        |-1 + 2    |1.0               |
        |2 * 2     | 4.0              |
        |4/2       | 2.0              |
        |1+(-2)    | -1.0             |
        |2 * (1+1) | 4.0              |
        |4/0       | Division by zero |
        |1-        | Syntax Error     |
    Scenario Outline: check assigning
      When input is "<input>"
      Then response is "<response>"
      Examples:
        | input                    |response                                |

        |x = 2 + 2, x              |4.0                                     |
        |x = 1, y = 2, 2 * (x + y) |6.0                                     |
        |x = 2, remove x           |Element successfully removed from memory|
