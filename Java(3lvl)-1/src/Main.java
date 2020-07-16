import java.util.ArrayList;

public class Main {



    public static void main(String[] args) {
        Apple ap1 = new Apple();
        Apple ap2 = new Apple();
        Orange or1 = new Orange();

        Box<Fruit> box1 = new Box<>();
        Box<Fruit> box2 = new Box<>();
        Box<Fruit> box3 = new Box<>();
        box1.add(ap1);
        box1.add(ap2);
        box1.add(ap1);
        box3.add(ap1);
        box2.add(or1);
        box2.add(or1);
        box1.add(or1);
        System.out.println(box2.getWeight());
        System.out.println(box2.getType());
        System.out.println(box1.compare(box2));
        System.out.println(box1.getWeight());
        System.out.println(box3.getWeight());
        box1.moving(box3);
        System.out.println(box1.getWeight());
        System.out.println(box3.getWeight());
        box1.moving(box3);
    }
}
