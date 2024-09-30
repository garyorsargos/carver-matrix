import { useContext } from "react";
import { GlobalContext } from "../context/GlobalContext";
import PageContainer from "../components/containers/PageContainer";

export const ChatBot: React.FC = () => {
  const { makeRequest } = useContext(GlobalContext);
  return (
    <PageContainer testId="chat-page">
      <div style={{ color: "#FFF" }}>Chat Bot Page</div>
      <button onClick={() => makeRequest.getTestResponse()}>
        Update Roles
      </button>
    </PageContainer>
  );
};

export default ChatBot;
