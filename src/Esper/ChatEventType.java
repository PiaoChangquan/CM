package Esper;

public class ChatEventType {
	String UserName;
	String ChatMessage;
	String GroupName;
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getChatMessage() {
		return ChatMessage;
	}
	public void setChatMessage(String chatMessage) {
		ChatMessage = chatMessage;
	}
	public String getGroupName() {
		return GroupName;
	}
	public void setGroupName(String groupName) {
		GroupName = groupName;
	}
	@Override
	public String toString() {
		return "ChatEventType [UserName=" + UserName + ", ChatMessage=" 
				+ ChatMessage + ", GroupName=" + GroupName+"]";
	}
}
