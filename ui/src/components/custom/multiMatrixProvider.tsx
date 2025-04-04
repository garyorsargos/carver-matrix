import React, { createContext, useContext, useState, ReactNode } from "react";

export type MultiMatrix = Map<string, Map<string, number>>;

// Add interface for image data
export interface MatrixImage {
  itemId: number;
  imageId: number;
  imageUrl: string;
}

export type ConfigType = {
  r2Multi: number;
  randomAssignment: boolean;
  roleBased: boolean;
  fivePointScoring: boolean;
  cMulti: number;
  aMulti: number;
  rMulti: number;
  vMulti: number;
  eMulti: number;
  description: string;
  name: string;
  // New fields for role-based matrices:
  hosts?: string[];
  participants?: string[];
  currentUserEmail?: string;
};

export type MultiMatrixContextType = {
  multiMatrix: MultiMatrix;
  setMultiMatrix: React.Dispatch<React.SetStateAction<MultiMatrix>>;
  config: ConfigType;
  setConfig: React.Dispatch<React.SetStateAction<ConfigType>>;
  itemIdMap: Map<string, number>;
  setItemIdMap: React.Dispatch<React.SetStateAction<Map<string, number>>>;
  updates: any[];
  setUpdates: React.Dispatch<React.SetStateAction<any[]>>;
  // New rawItems state to store the full items list from the API:
  rawItems: any[];
  setRawItems: React.Dispatch<React.SetStateAction<any[]>>;
  // New state for storing image data:
  itemImages: MatrixImage[];
  setItemImages: React.Dispatch<React.SetStateAction<MatrixImage[]>>;
  // Helper function to check if an item has images
  hasItemImages: (itemId: number) => boolean;
  // Helper function to get images for a specific item
  getItemImages: (itemId: number) => MatrixImage[];
};

const initialMultiMatrix: MultiMatrix = new Map([
  [
    "Nuclear Reactor",
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
    "Water Supply",
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
    "Apartment Building",
    new Map([
      ["Criticality", 1],
      ["Accessibility", 1],
      ["Recuperability", 1],
      ["Vulnerability", 1],
      ["Effect", 1],
      ["Recognizability", 1],
    ]),
  ],
]);

const initialConfig: ConfigType = {
  r2Multi: 1.0,
  randomAssignment: true,
  roleBased: false,
  fivePointScoring: true,
  cMulti: 1.0,
  aMulti: 1.0,
  rMulti: 1.0,
  vMulti: 1.0,
  eMulti: 1.0,
  description: "",
  name: "",
  hosts: [],
  participants: [],
  currentUserEmail: "",
};

const MultiMatrixContext = createContext<MultiMatrixContextType | undefined>(undefined);

export const useMultiMatrix = (): MultiMatrixContextType => {
  const context = useContext(MultiMatrixContext);
  if (!context) {
    throw new Error("useMultiMatrix must be used within a MultiMatrixProvider");
  }
  return context;
};

export const MultiMatrixProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [multiMatrix, setMultiMatrix] = useState<MultiMatrix>(initialMultiMatrix);
  const [config, setConfig] = useState<ConfigType>(initialConfig);
  const [itemIdMap, setItemIdMap] = useState<Map<string, number>>(new Map());
  const [updates, setUpdates] = useState<any[]>([]);
  const [rawItems, setRawItems] = useState<any[]>([]);
  const [itemImages, setItemImages] = useState<MatrixImage[]>([]);

  // Helper function to check if an item has associated images
  const hasItemImages = (itemId: number): boolean => {
    return itemImages.some(img => Number(img.itemId) === Number(itemId));
  };

  // Helper function to get all images for a specific item
  const getItemImages = (itemId: number): MatrixImage[] => {
    return itemImages.filter(img => Number(img.itemId) === Number(itemId));
  };

  const contextValue: MultiMatrixContextType = {
    multiMatrix,
    setMultiMatrix,
    config,
    setConfig,
    itemIdMap,
    setItemIdMap,
    updates,
    setUpdates,
    rawItems,
    setRawItems,
    itemImages,
    setItemImages,
    hasItemImages,
    getItemImages,
  };

  return (
    <MultiMatrixContext.Provider value={contextValue}>
      {children}
    </MultiMatrixContext.Provider>
  );
};

