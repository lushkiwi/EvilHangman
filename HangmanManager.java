/*  Student information for assignment:
 *
 *  On my honor, Shikhar Joshi, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Shikhar Joshi
 *  email address: shikharhjoshi@utexas.edu
 *  UTEID: shj577
 *  Section 5 digit ID: 50256
 *  Grader name:
 *  Number of slip days used on this assignment: 2
 */

// add imports as necessary

import java.util.*;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary during
 * rounds of hangman, based on guesses so far.
 *
 */
public class HangmanManager {

    // instance variables / fields
    private Set<String> dict;
    private boolean debug;
    private Set<String> activeWords;
    private Set<Character> guessedLetters;
    private String currPattern;
    private int guessesLeft;
    private int wordLen;
    private HangmanDifficulty difficulty;
    private int guessCounter;

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) {
        // throw error based on precondition
        if (words == null) {
            throw new IllegalArgumentException("Cannot pass null");
        }

        // initialize instance vars
        this.dict = new HashSet<>(words);
        this.debug = debugOn;
        this.guessedLetters = new TreeSet<>();
        this.activeWords = new HashSet<>();
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        // throw error based on precondition
        if (words == null) {
            throw new IllegalArgumentException("Cannot pass null");
        }

        // initialize instance vars
        this.dict = new HashSet<>(words);
        this.debug = false;
        this.guessedLetters = new TreeSet<>();
        this.activeWords = new HashSet<>();
    }


    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     * @param length The given length to check.
     * @return the number of words in the original Dictionary
     * with the given length
     */
    public int numWords(int length) {
        // initialize counter
        int count = 0;
        // foreach string in dict
        for (String s : dict) {
            // if the length of the string == parameter "length"
            if (s.length() == length) {
                // iterate counter
                count++;
            }
        }
        // return counter
        return count;
    }


    /**
     * Get for a new round of Hangman. Think of a round as a
     * complete game of Hangman.
     * @param wordLen the length of the word to pick this time.
     * numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the
     * player loses the round. numGuesses >= 1
     * @param diff The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        // check preconditions
        if (wordLen < 1 || numGuesses < 1 || numWords(wordLen) == 0 || diff == null) {
            throw new IllegalStateException("game parameters are invalid");
        }

        // initialize instance vars
        this.wordLen = wordLen;
        this.guessesLeft = numGuesses;
        this.difficulty = diff;
        this.guessCounter = 0;
        this.guessedLetters.clear();

        // initialize currPattern with dashes
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i < wordLen; i++) {
            patternBuilder.append("-");
        }
        this.currPattern = patternBuilder.toString();

        // populate activeWords with words of specified length
        this.activeWords.clear();
        for (String word : dict) {
            if (word.length() == wordLen) {
                activeWords.add(word);
            }
        }
    }


    /**
     * The number of words still possible (live) based on the guesses so far.
     *  Guesses will eliminate possible words.
     * @return the number of words that are still possibilities based on the
     * original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return activeWords.size();
    }


    /**
     * Get the number of wrong guesses the user has left in
     * this round (game) of Hangman.
     * @return the number of wrong guesses the user has left
     * in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return guessesLeft;
    }


    /**
     * Return a String that contains the letters the user has guessed
     * so far during this round.
     * The characters in the String are in alphabetical order.
     * The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     * @return a String that contains the letters the user
     * has guessed so far during this round.
     */
    public String getGuessesMade() {
        return guessedLetters.toString();
    }


    /**
     * Check the status of a character.
     * @param guess The characater to check.
     * @return true if guess has been used or guessed this round of Hangman,
     * false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        return guessedLetters.contains(guess);
    }


    /**
     * Get the current pattern. The pattern contains '-''s for
     * unrevealed (or guessed) characters and the actual character 
     * for "correctly guessed" characters.
     * @return the current pattern.
     */
    public String getPattern() {
        return currPattern;
    }


    /**
     * Update the game status (pattern, wrong guesses, word list),
     * based on the give guess.
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        // check if we have alr guessed
        if (alreadyGuessed(guess)) {
            throw new IllegalStateException("already guessed this word");
        }

        // add it to the guessed letters and iterate counter
        guessedLetters.add(guess);
        guessCounter++;

        // generate word families
        Map<String, Set<String>> patternToWords = new HashMap<>();
        // foreach word in the activeWords set
        for (String word : activeWords) {
            // generate a pattern for the word and guess
            // uses custom helper method
            String pattern = generatePattern(word, guess);

            // check if the map has the pattern
            Set<String> wordsForPattern = patternToWords.get(pattern);
            // doesnt have the pattern if above returns null
            if (wordsForPattern == null) {
                // create new hashset
                wordsForPattern = new HashSet<>();
                // put it in the map
                patternToWords.put(pattern, wordsForPattern);
            }
            wordsForPattern.add(word);
        }

        // select best pattern based on difficulty + tiebreakers
        // uses custom helper method
        String bestPattern = selectBestPattern(patternToWords, guess);

        // update gamestate
        activeWords = patternToWords.get(bestPattern);
        currPattern = bestPattern;

        // iterate guessesLeft down if necessary
        if (!currPattern.contains(String.valueOf(guess))) {
            guessesLeft--;
        }

        // prepare the return value
        TreeMap<String, Integer> result = new TreeMap<>();
        // foreach map entry in the map
        for (Map.Entry<String, Set<String>> entry : patternToWords.entrySet()) {
            // add to the result
            result.put(entry.getKey(), entry.getValue().size());
        }

        // return finally
        return result;
    }

    /**
     * generates pattern for a word based on guessed letter n current pattern
     * @param word: word to generate a pattern for
     * @param guess: guessed letter
     * @return generated pattern
     */
    private String generatePattern(String word, char guess) {
        // initialize stringbuilder
        StringBuilder newPattern = new StringBuilder();
        // for i from 0 to word length
        for (int i = 0; i < word.length(); i++) {
            // get the char @ i
            char c = word.charAt(i);
            // if that char == the guess, add it to the new pattern
            if (c == guess) {
                newPattern.append(guess);
            } else {
                // otherwise append the char @ i of currPattern (could be a letter or -)
                newPattern.append(currPattern.charAt(i));
            }
        }

        // return the string of the stringbuilder
        return newPattern.toString();
    }

    /**
     * selects best pattern based on difficulty and tie-breakers
     *
     * @param patternToWords: map of patterns to word sets
     * @param guess: the guessed letter
     * @return best pattern
     */
    private String selectBestPattern(Map<String, Set<String>> patternToWords, char guess) {
        // find the maximum word count
        int maxSize = 0;
        // foreach word in patternToWords
        for (Set<String> words : patternToWords.values()) {
            // update maxSize if > than current maxSize
            if (words.size() > maxSize) {
                maxSize = words.size();
            }
        }

        // get all the patterns w current max word count
        ArrayList<String> candidates = new ArrayList<>();
        // for each entry in patternToWords
        for (Map.Entry<String, Set<String>> entry : patternToWords.entrySet()) {
            // if its the same size as maxSize
            if (entry.getValue().size() == maxSize) {
                // add it to the list
                candidates.add(entry.getKey());
            }
        }

        // if there are > 1 candidates
        if (candidates.size() > 1) {
            // tiebreaker 1 from assignment - fewest occurences of guessed letter
            int minOccurrences = Integer.MAX_VALUE;
            // create a list to keep track
            ArrayList<String> fewestOccurrencesCandidates = new ArrayList<>();
            // foreach pattern in candidates
            for (String pattern : candidates) {
                // use helper method to count the number of occurences
                int occurrences = countOccurrences(pattern, guess);
                // if its less than current min
                if (occurrences < minOccurrences) {
                    // update minimum
                    minOccurrences = occurrences;
                    // clear the list
                    fewestOccurrencesCandidates.clear();
                    // add the pattern
                    fewestOccurrencesCandidates.add(pattern);
                } else if (occurrences == minOccurrences) {
                    // else if occurences == minimum, add the pattern
                    fewestOccurrencesCandidates.add(pattern);
                }
            }

            // reset candidates
            candidates = fewestOccurrencesCandidates;

            // tiebreaker 2 from assignment - alpha order
            if (candidates.size() > 1) {
                Collections.sort(candidates);
            }
        }

        // use difficulty to determine cadidates
        // default - first candidate
        String selectedPattern = candidates.get(0);
        if (difficulty == HangmanDifficulty.EASY) {
            if (guessCounter % 2 == 0 && candidates.size() > 1) {
                // pick second candidate if exists
                selectedPattern = candidates.get(1);
            }
        } else if (difficulty == HangmanDifficulty.MEDIUM) {
            if (guessCounter % 4 == 0 && candidates.size() > 1) {
                // pick second hardest every fourth guess
                selectedPattern = candidates.get(1);
            }
        }
        // hard difficulty - always pick the hardest (first candidate)

        return selectedPattern;
    }

    /**
     * Counts the number of times the guessed letter appears in the pattern.
     * @param pattern: the pattern to check.
     * @param guess: guessed letter.
     * @return num occurences
     */
    private int countOccurrences(String pattern, char guess) {
        // init counter
        int count = 0;
        // for each char in the pattern
        for (char c : pattern.toCharArray()) {
            // if the c is the guess, iterate counter
            if (c == guess) {
                count++;
            }
        }
        // return counter
        return count;
    }


    /**
     * Return the secret word this HangmanManager finally ended up
     * picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if (activeWords.isEmpty()) {
            throw new IllegalStateException("no more words left to choose from");
        }

        // if the size is 1, return the next one
        if (activeWords.size() == 1) {
            return activeWords.iterator().next();
        } else {
            // select a random word from activeWords
            ArrayList<String> wordList = new ArrayList<>(activeWords);
            Random rand = new Random();
            return wordList.get(rand.nextInt(wordList.size()));
        }
    }
}
