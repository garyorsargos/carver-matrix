import { createContext, useState } from "react";
import { Api } from "../api/api";

interface DefaultContext {
  roles: string[];
  makeRequest: Api;
}

export const defaultContext: DefaultContext = {
  roles: [],
  makeRequest: new Api(() => {}),
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
  const makeRequest = new Api(setRoles);

  return (
    <GlobalContext.Provider
      value={{
        roles: roles,
        makeRequest: makeRequest,
      }}
    >
      {children}
    </GlobalContext.Provider>
  );
};
