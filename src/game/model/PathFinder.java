package game.model;

import java.util.*;

public class PathFinder {
    private static class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        double gCost = 0; // Koszt drogi od startu
        double hCost = 0; // Szacowany koszt do celu
        double fCost = 0; // f = g + h

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost, other.fCost);
        }
    }

    public List<Cell> findPath(Cell startCell, Cell targetCell, GameBoard board) {
        if (startCell == null || targetCell == null) return null;
        if (startCell.getX() == targetCell.getX() && startCell.getY() == targetCell.getY()) return new ArrayList<>();

        Node startNode = new Node(startCell.getX(), startCell.getY());
        Node targetNode = new Node(targetCell.getX(), targetCell.getY());

        PriorityQueue<Node> openList = new PriorityQueue<>();
        Map<String, Node> closedList = new HashMap<>();

        startNode.hCost = calculateHeuristic(startNode, targetNode);
        startNode.fCost = startNode.hCost;
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            String currentKey = currentNode.x + "," + currentNode.y;
            closedList.put(currentKey, currentNode);

            // Cel osiągnięty?
            if (currentNode.x == targetNode.x && currentNode.y == targetNode.y) {
                return reconstructPath(currentNode, board);
            }

            // Sprawdzamy sąsiadów
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue; // Pomiń samego siebie

                    int nextX = currentNode.x + dx;
                    int nextY = currentNode.y + dy;

                    // 1. Sprawdzenie granic planszy
                    if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) continue;

                    Cell neighborCell = board.getCell(nextX, nextY);

                    // 2. Sprawdzenie czy pole to nie ściana
                    if (neighborCell.isWall()) continue;

                    // 3. ZABEZPIECZENIE: Jeśli pole jest zajęte przez innego agenta, omijamy je.
                    // Wyjątek: pozwalamy wejść, jeśli to pole to nasz ostateczny cel (targetCell).
                    if (neighborCell.getAgent() != null && !neighborCell.equals(targetCell)) {
                        continue;
                    }

                    Node neighbor = new Node(nextX, nextY);
                    String neighborKey = nextX + "," + nextY;
                    if (closedList.containsKey(neighborKey)) continue; // Już przetworzony

                    // Oblicz gCost
                    double moveCost = (dx != 0 && dy != 0) ? 1.414 : 1.0; // Koszt dla ruchu na skos vs prosto
                    double tentativeGCost = currentNode.gCost + moveCost;

                    Node openNode = getFromOpenList(openList, neighborKey);
                    if (openNode == null) {
                        openList.add(neighbor);
                        neighbor.parent = currentNode;
                        neighbor.gCost = tentativeGCost;
                        neighbor.hCost = calculateHeuristic(neighbor, targetNode);
                        neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    } else if (tentativeGCost < openNode.gCost) {
                        // Znaleziono lepszą drogę do istniejącego węzła
                        openNode.parent = currentNode;
                        openNode.gCost = tentativeGCost;
                        openNode.fCost = openNode.gCost + openNode.hCost;
                    }
                }
            }
        }
        return null; // Nie znaleziono ścieżki (np. cel jest całkowicie otoczony ścianami)
    }

    private Node getFromOpenList(PriorityQueue<Node> openList, String key) {
        for (Node n : openList) {
            if ((n.x + "," + n.y).equals(key)) return n;
        }
        return null;
    }

    private double calculateHeuristic(Node a, Node b) {
        // Dystans euklidesowy
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private List<Cell> reconstructPath(Node node, GameBoard board) {
        List<Cell> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(board.getCell(node.x, node.y));
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
}