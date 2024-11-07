package org.streamreasoning.sld.publishing.distribution;

import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

//@WebSocket
public class WebSocketHandler<E> implements Consumer<E> {

//    List<Session> sessionList = new ArrayList<>();
//
//    @OnWebSocketConnect
//    public void onConnect(Session s) throws Exception {
//        sessionList.add(s);
//        s.getRemote().sendString("Hello Colours");
//    }
//
//    @OnWebSocketClose
//    public void onClose(Session user, int statusCode, String reason) {
//        sessionList.remove(user);
//    }
//
//    @OnWebSocketMessage
//    public void onMessage(Session user, String message) {
//        sessionList.stream().filter(Session::isOpen).forEach(session -> {
//            try {
//                session.getRemote().sendString(message);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }


    @Override
    public void notify(DataStream<E> inputStream, E element, long timestamp) {
        //        onMessage(null, arg.toString());

    }
}
