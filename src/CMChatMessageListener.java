import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class CMChatMessageListener implements UpdateListener{

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		// TODO Auto-generated method stub
		if (newEvents != null) {
			for (int i = 0; i < newEvents.length; i++) {
				EventBean event = newEvents[i];
				System.out.println("Received NewEvents: " + event.getUnderlying());
				int theCount = Integer.parseInt(event.get("count(c)").toString());
//				System.out.println(theCount);
				if(!event.get("chatMessage").equals(event.get("groupName"))){
					if(theCount==11){
						System.out.println(!event.get("chatMessage").equals(event.get("groupName")));
						CMWinClientEventHandler.PushTheGroupInfo(event.get("chatMessage").toString());
					}
				}
				
			}
		} 
	}
}
