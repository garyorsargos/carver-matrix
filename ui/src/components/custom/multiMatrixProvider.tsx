import { createContext, useContext, useState } from "react";

type MultiMatrix = Map<string, Map<string, number>>;

type Config = {
  r2Multi: number;
  randomAssignment: boolean;
  roleBased: boolean;
  fivePointScoring: boolean;
  cmulti: number;
  amulti: number;
  rmulti: number;
  vmulti: number;
  emulti: number;
};

type MultiMatrixContextType = {
  multiMatrix: MultiMatrix;
  setMultiMatrix: React.Dispatch<React.SetStateAction<MultiMatrix>>;
  config: Config;
  setConfig: React.Dispatch<React.SetStateAction<Config>>;
};

const MultiMatrixContext = createContext<MultiMatrixContextType | undefined>(
  undefined,
);

export const useMultiMatrix = (): MultiMatrixContextType => {
  const context = useContext(MultiMatrixContext);
  if (!context) {
    throw new Error("useMultiMatrix must be used within a MultiMatrixProvider");
  }
  return context;
};

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

  const [config, setConfig] = useState<Config>({
    r2Multi: 1.0,
    randomAssignment: true,
    roleBased: false,
    fivePointScoring: true,
    cmulti: 1.0,
    amulti: 1.0,
    rmulti: 1.0,
    vmulti: 1.0,
    emulti: 1.0,
  });

  return (
    <MultiMatrixContext.Provider value={{ multiMatrix, setMultiMatrix, config, setConfig }}>
      {children}
    </MultiMatrixContext.Provider>
  );
};

