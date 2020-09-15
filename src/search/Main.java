package search;

import java.io.File;
import java.util.*;

public class Main {

	public static void main(String[] args) {
		String filename = "src/";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--data")) {
				filename = args[i+1];
			}
		}

		File file = new File(filename);
		Scanner scanner = new Scanner(System.in);

		try {
			ArrayList<String> people = initialize(new Scanner(file), new ArrayList<>());
			HashMap<String, HashSet<Integer>> invertedIndex = prepareInvertedIndex(people);

			System.out.println("=== menu ===");
			System.out.println("1. Find a person.\n2. Print all people.\n0. Exit");

			int option = scanner.nextInt();

			while (option != 0) {
				scanner.nextLine();
				switch (option) {
					case 0:
						break;
					case 1:
						System.out.println("Select a matching strategy: ALL, ANY, NONE");
						Finder finder = new Finder(FindStrategy.getStrategy(scanner.nextLine().toUpperCase()));

						System.out.println("Enter a name or email to search all suitable people.");
						String query = scanner.nextLine().toLowerCase().trim();

						System.out.println(finder.find(query, invertedIndex, people));
						break;
					case 2:
						System.out.println("=== List of people ===");
						for (String person : people) {
							System.out.println(person);
						}
						break;
				}
				System.out.println("=== menu ===");
				System.out.println("1. Find a person.\n2. Print all people.\n0. Exit");

				option = scanner.nextInt();
			}
			System.out.println("Bye!");
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	private static HashMap<String, HashSet<Integer>> prepareInvertedIndex(ArrayList<String> people) {
		HashMap<String, HashSet<Integer>> invertedIndex = new HashMap<>();
		for (int i = 0; i < people.size(); i++) {
			for(String key : people.get(i).toLowerCase().split(" ")){
				if (!invertedIndex.containsKey(key)) {
					invertedIndex.put(key, new HashSet<>());
				}
				invertedIndex.get(key).add(i);
			}
		}
		return  invertedIndex;
	}

	private static ArrayList<String> initialize(Scanner scanner, ArrayList<String> people) {
		while (scanner.hasNext()){
			people.add(scanner.nextLine());
		}

		return people;
	}
}

abstract class FindStrategy {
	public static FindStrategy getStrategy(String strategy) {
		switch (strategy) {
			case "ANY":
				return new FindAny();
			case "ALL":
				return new FindAll();
			case "NONE":
				return new FindNone();
		}
		throw new RuntimeException("not provided matching strategy");
	}
	public abstract String find(String query, HashMap<String, HashSet<Integer>> invertedIndex, ArrayList<String> people);
}

class FindAny extends FindStrategy {

	@Override
	public String find(String query, HashMap<String, HashSet<Integer>> invertedIndex, ArrayList<String> people) {

		StringBuilder found = new StringBuilder("");
		String[] queries = query.split(" ");

		for (String splitQuery: queries) {
			if (invertedIndex.containsKey(splitQuery)) {
				for (Integer index : invertedIndex.get(splitQuery)) {
					found.append(people.get(index) + "\n");
				}
			}
		}
		return found.toString().equals("") ? "No matching people found." : found.toString();
	}
}

class FindAll extends FindStrategy {

	@Override
	public String find(String query, HashMap<String, HashSet<Integer>> invertedIndex, ArrayList<String> people) {
		StringBuilder found = new StringBuilder("");
		String[] queries = query.split(" ");
		HashSet<Integer> result = null;

		for (String splitQuery: queries) {
			if (invertedIndex.containsKey(splitQuery)) {
				if (result == null) {
					result = new HashSet<>(invertedIndex.get(splitQuery));
					continue;
				}
				result.retainAll(invertedIndex.get(splitQuery));
			}
		}
		if (result == null) {
			return "No matching people found.";
		}

		for (Integer index : result) {
			found.append(people.get(index) + "\n");
		}

		return result.isEmpty() ? "No matching people found." : found.toString();
	}
}

class FindNone extends FindStrategy {

	@Override
	public String find(String query, HashMap<String, HashSet<Integer>> invertedIndex, ArrayList<String> people) {
		StringBuilder found = new StringBuilder("");
		HashSet<Integer> notResult = new HashSet<>();
		String[] queries = query.split(" ");

		for (String word : queries) {
			notResult.addAll(invertedIndex.get(word));
		}

		if (notResult.size() == people.size()) {
			return "No matching people found.";
		}

		for (int i = 0; i < people.size(); i++) {
			if (!notResult.contains(i)) {
				found.append(people.get(i) + "\n");
			}
		}
		return found.toString();
	}
}

class Finder {
	private FindStrategy findStrategy;
	public Finder(FindStrategy findStrategy){
		this.findStrategy = findStrategy;
	}

	public String find(String query, HashMap<String, HashSet<Integer>> invertedIndex, ArrayList<String> people) {
		return findStrategy.find(query, invertedIndex, people);
	}
}