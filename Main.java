package com.company;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        List<Element> list = new ArrayList<>();
        list.add(new Element(0.0, 0.0, 1));
        list.add(new Element(0.0, 1.0, 2));
        list.add(new Element(0.0, 2.0, 3));
        list.add(new Element(1.0, 0.0, 4));
        list.add(new Element(1.0, 1.0, 5));
        list.add(new Element(1.0, 2.0, 6));
        list.add(new Element(2.0, 0.0, 7));
        list.add(new Element(2.0, 1.0, 8));
        list.add(new Element(2.0, 2.0, 9));

        list.add(new Element(2.5, 2.5, 10));
//        list.add(new Element(3.0, 3.0, 11));
//        list.add(new Element(3.5, 3.5, 12));
//        list.add(new Element(4.2, 4.1, 13));

        // start point
        var a = new Element(0.0, 0.0);
        //var bX = new Element(1.0, 3.0);
        // target point
        var bX = new Element(3.0, 3.0);

        var maxRangeX = 1.1;
        var maxDistToTarget = 0.9;

        var listBX = new ArrayList<Element>();
        listBX.add(a);

        var setPrevious = new LinkedHashSet<Element>();
        setPrevious.add(a);

        var results = new ArrayList<LinkedHashSet<Element>>();

        var result = ok(list, listBX, maxRangeX, bX, a, setPrevious, ">>>>>> ", results, maxDistToTarget);

        System.out.println(results.size());

        var resultWithDist = result.stream().map(s -> {
            var linked = new LinkedList<Element>();
            var iter = s.iterator();
            while (iter.hasNext()) {
                linked.add(iter.next());
            }
            double distance = 0;
            for (int i = 0; i < linked.size() - 1; i++) {
                distance += distanceEuclidean(linked.get(i), linked.get(i + 1));
            }

            var tuple = new TupleDistVal();
            tuple.setDist(distance);
            tuple.setElements(s);
            return tuple;
        });

        var toSave = resultWithDist.collect(Collectors.toList());

        // TupleDistVal should implement Comaprable to correct use .sorted()
        toSave.stream().sorted().forEach(s -> {
            s.getElements().forEach(v -> {
                System.out.println(v.toString());
            });
            System.out.println("Distance:" + s.getDist());
        });


        var buffer = new StringBuffer();
        toSave.forEach(c->{
            c.getElements().forEach(s->{
               buffer.append(s.getX()+","+s.getY()+","+c.getDist()+"\n");
            });
        });

        try (PrintWriter out = new PrintWriter("filename.csv")) {
            out.println(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tuple contains results with comparator by dist
     */
    static class TupleDistVal implements Comparable<TupleDistVal> {
        private Double dist;
        private LinkedHashSet<Element> elements;

        public Double getDist() {
            return dist;
        }

        public void setDist(Double dist) {
            this.dist = dist;
        }

        public LinkedHashSet<Element> getElements() {
            return elements;
        }

        public void setElements(LinkedHashSet<Element> elements) {
            this.elements = elements;
        }

        @Override
        public int compareTo(TupleDistVal o) {
            return Double.compare(dist, o.dist);
        }
    }


    public static ArrayList<LinkedHashSet<Element>> ok(List<Element> list, List<Element> listB,
                                                       double maxRange, Element b, Element previous,
                                                       LinkedHashSet<Element> setPrevious, String lines,
                                                       ArrayList<LinkedHashSet<Element>> results,
                                                       double maxDistToTarget) {


        System.out.println(lines + "START METODY");
        listB.forEach(a -> {
            System.out.println(lines + "POCZATEK FOREACH, obecny elem: " + a.toString());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            var setPreviousInner = new LinkedHashSet<Element>();
            setPreviousInner.addAll(setPrevious);
            setPreviousInner.forEach(s -> System.out.println(lines + s.toString()));
            setPreviousInner.add(a);
            var a1 = searchClosest(list, a, maxRange, previous, setPreviousInner);
            var distToTarget = distanceEuclidean(a, b);
            //////////if (a.getId() == 10) System.out.println("huj " + distToTarget);

            if (distToTarget <= maxDistToTarget) {
                System.out.println(lines + "ZNALEZIONO DROGE+++++++++++++++++++++");
                setPreviousInner.forEach(s -> System.out.println(s.toString()));
                results.add(setPreviousInner);
            }

            System.out.println(lines + "~~~~~~~~~~~~~~~~~~~~~~~~");
            a1.forEach(s -> System.out.println(lines + s.toString()));
            System.out.println(lines + "~~~~~~~~~~~~~~~~~~~~~~~~");
            if (a1.size() == 0) {
                System.out.println(lines + "BRAK ELEMENTOW------------------------");
            } else if (a1.size() > 0) {
                ///var maxDistToTarget = 1.5;
                var prewNext = new LinkedHashSet<Element>();
                prewNext.addAll(setPreviousInner);
                String line = lines + " >>>>>>>> ";
                ok(list, a1, maxRange, b, a, prewNext, line, results, maxDistToTarget);

            }
            System.out.println(lines + "KONIEC FOREACH, obecny elem: " + a.toString());
        });
        System.out.println(lines + "KONIEC METODY");

        return results;
    }

    /**
     * Find all initial closest elements in a given range
     *
     * @param list
     * @param element
     * @param range
     * @return
     */
    public static List<Element> searchClosest(List<Element> list, Element element, double range, Element toRemove, LinkedHashSet<Element> toRemoveSet) {
        var closest = new ArrayList<Element>();
        var removedList = new ArrayList<>(list);
        removedList.removeAll(toRemoveSet);
        for (Element s : removedList) {
            if (!compareElements(s, toRemove)) {
                var dist = distanceEuclidean(s, element);
                //////////if (element.getId() == 10) System.out.println(element.toString() + "kurwa " + s.toString());
                if (dist <= range) {
                    // prevent adding self
                    if (dist != 0) closest.add(s);
                }
            }
        }
        return closest;
    }


    /**
     * Find closest element/s
     *
     * @param list
     * @param element
     * @return
     */
    public static List<Element> findClosestElements(List<Element> list, Element element) {

        var distElements = new HashMap<Double, ArrayList<Element>>();
        for (Element s : list) {
            var dist = distanceEuclidean(s, element);
            if (distElements.containsKey(dist)) {
                var existingList = distElements.get(dist);
                existingList.add(s);
                distElements.put(dist, existingList);
            } else {
                distElements.put(dist, new ArrayList(Arrays.asList(s)));
            }
        }

        var distClosest = Double.POSITIVE_INFINITY;
        for (Double dist : distElements.keySet()) {
            distClosest = dist < distClosest ? dist : distClosest;
        }

        return distElements.get(distClosest);
    }

    public static boolean compareElements(Element a, Element b) {
        return a.getY() == b.getY() && a.getX() == b.getX();
    }

    public static double distanceEuclidean(Element a, Element b) {
        return distanceEuclidean(new double[]{a.getX(), a.getY()}, new double[]{b.getX(), b.getY()});
    }

    public static double distanceEuclidean(double[] t1, double[] t2) {
        double sum = 0;
        for (int i = 0; i < t1.length; i++) {
            sum += Math.pow((t1[i] - t2[i]), 2.0);
        }
        return Math.sqrt(sum);
    }


    public static class Element {
        private Double x;
        private Double y;
        private int id = 0;
        private boolean action = false;

        public Element(Double x, Double y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }

        public Element(Double x, Double y) {
            this.x = x;
            this.y = y;
        }

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isAction() {
            return action;
        }

        public void setAction(boolean action) {
            this.action = action;
        }

        // comparing by x,y only
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Element element = (Element) o;
            return x == element.x &&
                    y == element.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Element{" +
                    "x=" + x +
                    ", y=" + y +
                    ", id=" + id +
                    ", action=" + action +
                    '}';
        }
    }
}
