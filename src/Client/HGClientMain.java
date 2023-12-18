package Client;

public class HGClientMain {
    CSUser csUser;

    RandomName randomName;

    JF_Login jf_login;
    JF_Robby jf_robby;
    JF_ReadyRoom jf_readyRoom;
    JF_PlayGame jf_playGame;

    public HGClientMain() {
        randomName = new RandomName();

        jf_login = new JF_Login(this);
        jf_robby = new JF_Robby(this);
        jf_readyRoom = new JF_ReadyRoom(this);
        jf_playGame = new JF_PlayGame(this);

        ResetFrame();
        jf_login.setVisible(true);
    }

    void ResetFrame(){
        jf_login.setVisible(false);
        jf_robby.setVisible(false);
        jf_readyRoom.setVisible(false);
        jf_playGame.setVisible(false);
    }

    public static void main(String[] args){
        new HGClientMain();
    }
}
