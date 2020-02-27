package com.netease.wulewan.uikit.api;

import java.util.ArrayList;

public class CustomEventManager {



    private static CustomEventManager instance = new CustomEventManager();

    public static CustomEventManager getInstance() {
        return instance;
    }

    private ArrayList<CustomListener> listeners = new ArrayList<>();
    /**
     * 添加事件
     *
     * @param listener
     *            DoorListener
     */
    public  void addCustomListener(CustomListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);

    }

    /**
     * 移除事件
     *
     * @param listener
     *            DoorListener
     */
    public  void removeCustomListener(CustomListener listener) {
        if (listener == null)
            return;
        listeners.remove(listener);
    }



    /**
     * 通知所有的Listener
     */
    public  void notifyListeners(CustomEvent event) {
        if (event == null)
            return;
        for (CustomListener listener : listeners) {
            if (listener.getName() == event.getName()){
                listener.execute(event);
            }
        }
    }


    public static class CustomEvent {
        private CustomListenerName name ;
        private Object data;
        public CustomEvent(CustomListenerName name,Object data) {
            this.name = name;
            this.data = data;
        }

        public CustomListenerName getName() {
            return name;
        }

        public Object getData() {
            return data;
        }
    }

    public static enum CustomListenerName {
        SHARECREATESESSION,
        SHARESELECTDATABACK,
    }

    public static class CustomListener {
         private  CustomListenerName name;

         public CustomListener(CustomListenerName name){
            this.name = name;
         }

        public CustomListenerName getName() {
            return name;
        }

        public void execute(CustomEvent event){

        }
    }

}
