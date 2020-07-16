import java.util.ArrayList;
import java.util.Arrays;

public class Box<T extends Fruit> {
    private final ArrayList<T> Array = new ArrayList<>();
    private T box;


    /*public Box(T... array) {
        Array.addAll(Arrays.asList(array));
    }

    public Box(T... array){
        for (int i = 0; i < array.length; i++) {
            Array.add(array[i]);
        }
    }

    public void replace(int first, int second){
        T TestBox = this.getArr().get(first);
        this.getArr().set(first, this.getArr().get(second));
        this.getArr().set(second, TestBox);

    }
    */
    public Box(){
    }

    public ArrayList<T> getArr(){
        return this.Array;
    }

    public double getWeight(){
        double weight = 0;
        for (T t : Array) {
            weight += t.getWeight();
        }
        return weight;
    }

    public boolean compare(Box<T> box) {
        return (box.getWeight() == this.getWeight());
    }

    public void moving(Box<T> box){
        if(this.getArr().isEmpty()) {
            System.out.println("Коробка пуста");

        } else if(box.getType().equals(this.getType()) || box.getType().equals("empty")){
            box.getArr().addAll(this.getArr());
            this.getArr().clear();
        }
    }



    public void add(T obj){
        if(obj.getType().equals(this.getType()) || this.getType().equals("empty"))
        Array.add(obj);
        else System.out.println("Can't add " + obj.getType() + " to " + this.toString());
    }

    public String getType() {
        if (Array.size() > 0)
        return (Array.get(0).getType());
        else return "empty";
    }

}
