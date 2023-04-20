package cs505finaltemplate.CEP;

import cs505finaltemplate.Launcher;

import io.siddhi.core.util.transport.InMemoryBroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OutputSubscriber implements InMemoryBroker.Subscriber {

    private String topic;

    public OutputSubscriber(String topic, String streamName) {
        this.topic = topic;
    }

    @Override
    public void onMessage(Object msg) {

        try {
            System.out.println("OUTPUT CEP EVENT: " + msg);
            System.out.println("");

            //You will need to parse output and do other logic
            Launcher.lastCEPOutput = String.valueOf(msg);
            String[] sstr = String.valueOf(msg).split("\\D+");
	        Map<String, Integer> tempList = new HashMap<String, Integer>();
	        for (int i = 1; i < sstr.length; i+=2){
		        tempList.put(sstr[i], Integer.parseInt(sstr[i+1]));
	        }

            Launcher.alerts = new ArrayList<>();
            for (Map.Entry<String, Integer> element : tempList.entrySet()) {
                Integer prevCount = Launcher.CEPList.get(element.getKey());
                if (prevCount != null) {
                    //Only raise alert status if there is a growth factor of 2!~
                    if (element.getValue() >= prevCount*2) {
                        Launcher.alerts.add(element.getKey());
                    }
                }
            }
            Launcher.CEPList = tempList;

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public String getTopic() {
        return topic;
    }

}
