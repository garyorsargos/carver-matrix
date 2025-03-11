import { createContext, useState, useEffect } from "react";
import { Api } from "../api/api";
import axios from "axios";

interface DefaultContext {
  roles: string[];
  makeRequest: Api;
  userId: string | null;
  username: string | null;
}

export const defaultContext: DefaultContext = {
  roles: [],
  makeRequest: new Api(() => {}),
  userId: null,
  username: null,
};

export const GlobalContext = createContext<DefaultContext>(defaultContext);

interface GlobalContextProps {
  children: React.ReactElement | React.ReactElement[];
}

/**
 * Provides the entire application with instant access to
 * key/important information without the need to prop drill.
 * Values can be added by updating the "DefaultContext"
 * interface, the defaultContext object, and finally the
 * "value" attribute of the GlobalContext.Provider component.
 *
 * @param children React Element or Elements
 * @returns Passed in Children
 */
export const ContextProvider: React.FC<GlobalContextProps> = ({ children }) => {
  const [roles, setRoles] = useState<string[]>([]);
  const [userId, setUserId] = useState<string | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const makeRequest = new Api(setRoles);

  useEffect(() => {
    const fetchWhoami = async () => {
      try {
        const response = await axios.get(
          "http://localhost:9002/api/user2/whoami-upsert",
          { withCredentials: true }
        );
        const { userId, username } = response.data;
        setUserId(userId);
        setUsername(username);
      } catch (error) {
        console.error("Error fetching user info:", error);
      }
    };
    fetchWhoami();
  }, []);

  return (
    <GlobalContext.Provider
      value={{
        roles: roles,
        makeRequest: makeRequest,
        userId: userId,
        username: username,
      }}
    >
      {children}
    </GlobalContext.Provider>
  );
};
