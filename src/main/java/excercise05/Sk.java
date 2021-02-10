package excercise05;

public class Sk extends state{



    public void onStart() {
        this.init();




    }

   public void onSelection() {
        System.out.println(this.getMailbox());
        this.initiator = true;
        this.test();

        }

}
