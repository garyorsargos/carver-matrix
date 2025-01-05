import { createContext, useContext, useState } from "react";

// Define the MultiMatrix type
type MultiMatrix = Map<string, Map<string, number>>;

// Define the context type
type MultiMatrixContextType = {
  multiMatrix: MultiMatrix;
  setMultiMatrix: React.Dispatch<React.SetStateAction<MultiMatrix>>;
};

// Create the context
const MultiMatrixContext = createContext<MultiMatrixContextType | undefined>(
  undefined,
);

// Hook to use the MultiMatrix context
export const useMultiMatrix = (): MultiMatrixContextType => {
  const context = useContext(MultiMatrixContext);
  if (!context) {
    throw new Error("useMultiMatrix must be used within a MultiMatrixProvider");
  }
  return context;
};

// Provider Component
export const MultiMatrixProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [multiMatrix, setMultiMatrix] = useState<MultiMatrix>(
    new Map([
      [
        "Example Target 1",
        new Map([
          ["Criticality", 1],
          ["Accessibility", 1],
          ["Recuperability", 1],
          ["Vulnerability", 1],
          ["Effect", 1],
          ["Recognizability", 1],
        ]),
      ],
      [
        "Example Target 2",
        new Map([
          ["Criticality", 1],
          ["Accessibility", 1],
          ["Recuperability", 1],
          ["Vulnerability", 1],
          ["Effect", 1],
          ["Recognizability", 1],
        ]),
      ],
      [
        "Example Target 3",
        new Map([
          ["Criticality", 1],
          ["Accessibility", 1],
          ["Recuperability", 1],
          ["Vulnerability", 1],
          ["Effect", 1],
          ["Recognizability", 1],
        ]),
      ],
    ]),
  );

  return (
    <MultiMatrixContext.Provider value={{ multiMatrix, setMultiMatrix }}>
      {children}
    </MultiMatrixContext.Provider>
  );
};
