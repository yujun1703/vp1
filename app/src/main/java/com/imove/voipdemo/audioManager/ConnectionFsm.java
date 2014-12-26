package com.imove.voipdemo.audioManager;

/**
 * Created by zhangyun on 14/12/24.
 */



import com.imove.voipdemo.audioManager.FSM.EventHandler;
import com.imove.voipdemo.audioManager.FSM.FsmEvent;
import com.imove.voipdemo.audioManager.FSM.FsmState;
import com.imove.voipdemo.config.CommonConfig;

import java.net.Socket;


public class ConnectionFsm  {
    // defines the state events


    FsmState<Events> init;
    FsmState<Events> request;
    FsmState<Events> requestret;
    FsmState<Events> requestreply;
    FsmState<Events> accept;
    FsmState<Events> acceptreturn;
    FsmState<Events> connction;
    FsmState<Events> close;

    FSM<Events> fsm;
    private ServerSocket ss;


    public void ConnectionFsm()
    {
        ss=ServerSocket.getServerSocketInstance();
    }



    private enum Events implements FSM.FsmEvent<Events> {

        INIT,
        CREATE_SESSION_REQUEST,
        CREATE_SESSION_REQUEST_RETURN,
        CREATE_SESSION_REQUEST_REPLY,

        CREATE_SESSION_ACCEPT,
        CREATE_SESSION_ACCEPT_RETRUN,

        CONNECTION,//打开语音发送与接收
        CLOSE;

        @Override
        public Events getType() {
            return this;
        }
    };



    public void createfsm() {
        // defines an echo state that prints messages to the console
        init = new FsmState<Events>("INIT", Events.class);
        init.addHandler(Events.INIT, new EventHandler<Events>() {
            private int counter;

            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("%s %s\n", evt, counter++);
            }
        });

        request = new FsmState<Events>("CREATE_SESSION_REQUEST", Events.class, null, null, new EventHandler<Events>() {
            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("CREATE_SESSION_REQUEST %s \n", evt);
                ss.CreateSession(CommonConfig.USER_ACTION_REQUEST);
                //fsm.deliver(requestret);
            }
        });

        requestret = new FsmState<Events>("CREATE_SESSION_REQUEST_RETURN", Events.class, null, null, new EventHandler<Events>() {
            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("CREATE_SESSION_REQUEST %s \n", evt);
            }
        });

        requestreply = new FsmState<Events>("CREATE_SESSION_REQUEST_REPLY", Events.class, null, null, new EventHandler<Events>() {
            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("CREATE_SESSION_REQUEST %s \n", evt);
            }
        });

        accept = new FsmState<Events>("CREATE_SESSION_ACCEPT", Events.class, null, null, new EventHandler<Events>() {
            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("CREATE_SESSION_REQUEST %s \n", evt);
            }
        });

        acceptreturn = new FsmState<Events>("CREATE_SESSION_ACCEPT_RETRUN", Events.class, null, null, new EventHandler<Events>() {
            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("CREATE_SESSION_REQUEST %s \n", evt);
            }
        });

        connction = new FsmState<Events>("CONNECTION", Events.class, null, null, new EventHandler<Events>() {
            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("CREATE_SESSION_REQUEST %s \n", evt);
            }
        });

        // defines a closed state that logs warnings from the default handler
        close = new FsmState<Events>("CLOSE", Events.class, null, null, new EventHandler<Events>() {
            @Override
            public void handleEvent(FsmEvent<Events> evt) {
                System.out.printf("Ignoring %s as the state machine is closed\n", evt);
            }
        });

        // creates the state machine
        fsm = new FSM<Events>(init);

        // adds a state transition to the closed state
        init.addTransition(request, Events.CREATE_SESSION_REQUEST);
        //init.addTransition(request, Events.CREATE_SESSION_REQUEST);


        /*
        fsm.deliver(Events.MSG);
        fsm.deliver(Events.MSG);
        fsm.deliver(Events.CLOSE);
        fsm.deliver(Events.MSG);
        */
    }
}