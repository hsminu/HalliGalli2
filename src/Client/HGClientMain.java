package Client;

public class HGClientMain {

    CSUser csUser;

    JF_Login jf_login;
    JF_WaitRoom jf_waitRoom;
    JF_ReadyRoom jf_readyRoom;
    JF_PlayGame jf_playGame;

    public HGClientMain() {
        jf_login = new JF_Login(this);
        jf_waitRoom = new JF_WaitRoom(this);
        jf_readyRoom = new JF_ReadyRoom(this);
        jf_playGame = new JF_PlayGame(this);

        ResetFrame();
        display("JF_Login");
    }

    void display(String viewName){
        if(viewName.equals("JF_Login")){
            ResetFrame();
            jf_login.setVisible(true);
        }
        if(viewName.equals("JF_WaitRoom")){
            ResetFrame();
            jf_waitRoom.setVisible(true);
        }
        if(viewName.equals("JF_ReadyRoom")){
            jf_readyRoom.setVisible(true);
        }
        if(viewName.equals("JF_PlayGame")){
            ResetFrame();
            jf_playGame.setVisible(true);
        }
    }

    void ResetFrame(){
        jf_login.setVisible(false);
        jf_waitRoom.setVisible(false);
        jf_playGame.setVisible(false);
    }

    public static void main(String[] args){
        new HGClientMain();
    }
}
