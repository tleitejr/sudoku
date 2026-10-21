package com.dio.service;

import com.dio.model.Difficulty;
import com.dio.model.Puzzle;
import com.dio.util.GridMath;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Parses puzzles. Line format: {@code nome;DIFICULDADE;81 caracteres} (0 or '.' = empty),
 * or just the 81 characters (used for command-line arguments).
 */
public final class PuzzleLoader {

    public static final String DEFAULT_RESOURCE = "/puzzles/puzzles.txt";
    private static final String CLI_NAME = "Linha de comando";

    private PuzzleLoader() {
    }

    public static Puzzle parseLine(String line) throws PuzzleFormatException {
        String[] parts = line.trim().split(";", -1);
        if (parts.length != 1 && parts.length != 3) {
            throw new PuzzleFormatException("Formato inválido. Use 'nome;DIFICULDADE;81 caracteres'.");
        }
        String name = CLI_NAME;
        Difficulty difficulty = Difficulty.MEDIO;
        if (parts.length == 3) {
            name = parts[0].trim();
            if (name.isEmpty()) {
                throw new PuzzleFormatException("O nome do puzzle não pode ser vazio.");
            }
            difficulty = parseDifficulty(parts[1].trim());
        }
        return new Puzzle(name, difficulty, parseGrid(parts[parts.length - 1].trim()));
    }

    public static List<Puzzle> parseAll(Reader reader) throws IOException, PuzzleFormatException {
        List<Puzzle> puzzles = new ArrayList<>();
        BufferedReader buffered = new BufferedReader(reader);
        String line;
        int number = 0;
        while ((line = buffered.readLine()) != null) {
            number++;
            if (line.isBlank() || line.trim().startsWith("#")) {
                continue;
            }
            try {
                puzzles.add(parseLine(line));
            } catch (PuzzleFormatException e) {
                throw new PuzzleFormatException("Linha " + number + ": " + e.getMessage());
            }
        }
        return puzzles;
    }

    public static List<Puzzle> loadFromResource(String resource) throws IOException, PuzzleFormatException {
        try (InputStream in = PuzzleLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Recurso não encontrado: " + resource);
            }
            return parseAll(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    /** Uses the first argument that looks like a puzzle; empty when there are no arguments. */
    public static Optional<Puzzle> fromArgs(List<String> args) throws PuzzleFormatException {
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                return Optional.of(parseLine(arg));
            }
        }
        return Optional.empty();
    }

    public static Optional<Puzzle> pickRandom(List<Puzzle> puzzles, Difficulty difficulty, Random random) {
        List<Puzzle> candidates = puzzles.stream().filter(p -> p.difficulty() == difficulty).toList();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    private static Difficulty parseDifficulty(String text) throws PuzzleFormatException {
        try {
            return Difficulty.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PuzzleFormatException("Dificuldade desconhecida: '" + text + "' (use FACIL, MEDIO ou DIFICIL).");
        }
    }

    private static int[] parseGrid(String text) throws PuzzleFormatException {
        if (text.length() != GridMath.CELLS) {
            throw new PuzzleFormatException(
                    "O tabuleiro deve ter " + GridMath.CELLS + " caracteres, mas tem " + text.length() + ".");
        }
        int[] grid = new int[GridMath.CELLS];
        for (int i = 0; i < GridMath.CELLS; i++) {
            char ch = text.charAt(i);
            if (ch == '.') {
                grid[i] = 0;
            } else if (ch >= '0' && ch <= '9') {
                grid[i] = ch - '0';
            } else {
                throw new PuzzleFormatException("Caractere inválido '" + ch + "' na posição " + (i + 1) + ".");
            }
        }
        return grid;
    }
}
