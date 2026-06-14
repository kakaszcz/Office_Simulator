package game.model;

import java.util.*;

/**
 * Algorytm wyszukiwania ścieżek realizujący podejście A* (A-Star).
 * Wyznacza najkrótszą i najbardziej optymalną drogę dla agentów poruszających się
 * po siatce dwuwymiarowej biura, uwzględniając przeszkody stałe (ściany), obecność
 * innych pracowników oraz specyfikę kosztu ruchu po skosie (odległość euklidesowa).
 */
public class PathFinder {

    /**
     * Wewnętrzna klasa reprezentująca węzeł (grafowy odpowiednik kafelka) w przestrzeni algorytmu A*.
     * Implementuje interfejs {@link Comparable} w celu automatycznego sortowania w kolejce priorytetowej.
     */
    private static class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        double gCost = 0;
        double hCost = 0;
        double fCost = 0;

        /**
         * Tworzy nowy węzeł na podstawie współrzędnych planszy.
         *
         * @param x Pozycja pozioma węzła.
         * @param y Pozycja pionowa węzła.
         */
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Porównuje dwa węzły na podstawie ich całkowitego szacowanego kosztu ścieżki (fCost).
         * Służy do wyznaczania węzła o najniższym koszcie na szczycie kolejki priorytetowej.
         *
         * @param other Drugi węzeł do porównania.
         * @return Wynik porównania wartości fCost (wartość ujemna, zero lub dodatnia).
         */
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost, other.fCost);
        }
    }

    /**
     * Wyznacza optymalną ścieżkę (sekwencję kafelków) od komórki startowej do docelowej.
     * Algorytm dynamicznie wylicza koszt przejścia dla 8 kierunków (krok prosty = 1.0, skos = ~1.414).
     * Zabezpiecza wejście na kafelek docelowy będący obiektem interaktywnym (np. biurko, ekspres do kawy).
     *
     * @param startCell Komórka, z której agent rozpoczyna ruch.
     * @param targetCell Komórka docelowa, do której agent zmierza.
     * @param board Logiczna plansza gry, na której odbywa się ruch.
     * @return Lista komórek {@link Cell} tworzących uporządkowaną ścieżkę lub null, jeśli droga nie istnieje.
     */
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

                    // Komórka docelowa zawsze jest dozwolona (np. ekspres do kawy, trawa)
                    boolean isTarget = neighborCell.equals(targetCell);
                    if (!isTarget && !neighborCell.isWalkable()) continue;

                    // Jeśli na kafelku stoi agent i to nie jest cel, omijamy
                    if (neighborCell.getAgent() != null && !isTarget) continue;

                    String neighborKey = nextX + "," + nextY;
                    if (closedList.containsKey(neighborKey)) continue;

                    Node neighbor = new Node(nextX, nextY);
                    double moveCost = (dx != 0 && dy != 0) ? 1.414 : 1.0;
                    double tentativeGCost = currentNode.gCost + moveCost;

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

    /**
     * Oblicza euklidesową odległość geometryczną (heurystykę) między dwoma węzłami.
     * Służy jako szacunkowy koszt minimalny pozostały do osiągnięcia punktu docelowego.
     *
     * @param a Węzeł źródłowy (bieżący sąsiad).
     * @param b Węzeł docelowy.
     * @return Wartość typu double reprezentująca dystans w linii prostej.
     */
    private double calculateHeuristic(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    /**
     * Odtwarza kompletną ścieżkę kafelków poprzez cofanie się od węzła końcowego
     * do punktu startowego przy użyciu referencji na węzły nadrzędne (parents).
     * Wynikowa lista jest automatycznie odwracana do właściwej kolejności ruchu.
     *
     * @param node Węzeł docelowy, do którego algorytm pomyślnie dotarł.
     * @param board Logiczna plansza gry potrzebna do mapowania współrzędnych na obiekty Cell.
     * @return Uporządkowana chronologicznie lista kafelków prowadząca do celu.
     */
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
