package game.model;

import java.util.*;

public class PathFinder {
    private static class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        double gCost = 0;
        double hCost = 0;
        double fCost = 0;

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

        Map<String, Node> openListMap = new HashMap<>();
        Map<String, Node> closedList = new HashMap<>();

        startNode.hCost = calculateHeuristic(startNode, targetNode);
        startNode.fCost = startNode.hCost;
        openList.add(startNode);
        openListMap.put(startNode.x + "," + startNode.y, startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            String currentKey = currentNode.x + "," + currentNode.y;
            openListMap.remove(currentKey);
            closedList.put(currentKey, currentNode);

            if (currentNode.x == targetNode.x && currentNode.y == targetNode.y) {
                return reconstructPath(currentNode, board);
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    int nextX = currentNode.x + dx;
                    int nextY = currentNode.y + dy;

                    if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) continue;

                    Cell neighborCell = board.getCell(nextX, nextY);

                    if (neighborCell.isWall() && !neighborCell.equals(targetCell)) continue;

                    if (neighborCell.getAgent() != null && !neighborCell.equals(targetCell)) {
                        // Jeśli na kafelku obok siedzi ktoś przy biurku ("desk"), traktujemy to jako stałą ścianę
                        if ("desk".equals(neighborCell.getType())) {
                            continue;
                        }
                    }

                    Node neighbor = new Node(nextX, nextY);
                    String neighborKey = nextX + "," + nextY;
                    if (closedList.containsKey(neighborKey)) continue;

                    double moveCost = (dx != 0 && dy != 0) ? 1.414 : 1.0;
                    double tentativeGCost = currentNode.gCost + moveCost;

                    // ZMIANA: Błyskawiczne pobieranie z mapy zamiast powolnej pętli po kolejce
                    Node openNode = openListMap.get(neighborKey);

                    if (openNode == null) {
                        neighbor.parent = currentNode;
                        neighbor.gCost = tentativeGCost;
                        neighbor.hCost = calculateHeuristic(neighbor, targetNode);
                        neighbor.fCost = neighbor.gCost + neighbor.hCost;

                        openList.add(neighbor);
                        openListMap.put(neighborKey, neighbor);
                    } else if (tentativeGCost < openNode.gCost) {

                        openList.remove(openNode);

                        openNode.parent = currentNode;
                        openNode.gCost = tentativeGCost;
                        openNode.fCost = openNode.gCost + openNode.hCost;

                        openList.add(openNode);
                    }
                }
            }
        }
        return null;
    }

    private double calculateHeuristic(Node a, Node b) {
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