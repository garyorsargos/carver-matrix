// MatrixLoader.tsx
import React, { useEffect } from "react";
import axios from "axios";
import { useMultiMatrix } from "./multiMatrixProvider";

const MatrixLoader: React.FC = () => {
  const { setMultiMatrix, setConfig } = useMultiMatrix();

  useEffect(() => {
    // Parse the matrixId query parameter from the URL.
    const params = new URLSearchParams(window.location.search);
    const matrixId = params.get("matrixId");

    if (!matrixId) {
      console.warn("matrixId query parameter is missing.");
      return;
    }

    // Request the matrix data as text because the response might be concatenated JSON.
    axios
      .get(`/api/carvermatrices/${matrixId}`, { responseType: "text" })
      .then((response) => {
        const dataString = response.data;
        let matrixData;

        try {
          // Check if there are two concatenated JSON objects.
          if (dataString.includes("}{")) {
            const parts = dataString.split("}{");
            matrixData = JSON.parse(parts[0] + "}");
          } else {
            matrixData = JSON.parse(dataString);
          }
        } catch (e) {
          console.error("Failed to parse matrix data:", e);
          return;
        }

        console.log("Parsed matrix data:", matrixData);

        // Ensure the matrixData contains an items array.
        if (!matrixData.items || !Array.isArray(matrixData.items)) {
          console.error("Matrix data does not contain a valid items array:", matrixData);
          return;
        }

        // Update the config using the correct property names.
        setConfig({
          r2Multi: matrixData.r2Multi,
          randomAssignment: matrixData.randomAssignment,
          roleBased: matrixData.roleBased,
          fivePointScoring: matrixData.fivePointScoring,
          cmulti: matrixData.cMulti,
          amulti: matrixData.aMulti,
          rmulti: matrixData.rMulti,
          vmulti: matrixData.vMulti,
          emulti: matrixData.eMulti,
          description: matrixData.description,
          name: matrixData.name,
        });

        // Build the MultiMatrix Map from the items array.
        const matrixMap = new Map<string, Map<string, number>>();
        matrixData.items.forEach((item: any) => {
          matrixMap.set(
            item.itemName,
            new Map([
              ["Criticality", item.criticality],
              ["Accessibility", item.accessibility],
              ["Recuperability", item.recoverability],
              ["Vulnerability", item.vulnerability],
              ["Effect", item.effect],
              ["Recognizability", item.recognizability],
            ])
          );
        });

        setMultiMatrix(matrixMap);
      })
      .catch((error) => {
        console.error("Error fetching matrix data:", error);
      });
  }, [setMultiMatrix, setConfig]);

  return null;
};

export default MatrixLoader;

