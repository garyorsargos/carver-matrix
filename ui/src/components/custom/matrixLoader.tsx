import React, { useEffect } from "react";
import axios from "axios";
import { useMultiMatrix } from "./multiMatrixProvider";

const MatrixLoader: React.FC = () => {
  const { setMultiMatrix, setConfig, setItemIdMap, setRawItems } = useMultiMatrix();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const matrixId = params.get("matrixId");

    if (!matrixId) {
      console.warn("matrixId query parameter is missing.");
      return;
    }

    // First, get the current user information (specifically the email)
    axios
      .get("/api/user2/whoami-upsert", { responseType: "text" })
      .then((userResponse) => {
        const userDataString = userResponse.data;
        let userData;
        try {
          if (userDataString.includes("}{")) {
            const parts = userDataString.split("}{");
            userData = JSON.parse(parts[0] + "}");
          } else {
            userData = JSON.parse(userDataString);
          }
        } catch (e) {
          console.error("Failed to parse user data:", e);
          return;
        }
        const currentUserEmail = userData.email;
        console.log("Current user email:", currentUserEmail);

        // Now, fetch the matrix data
        axios
          .get(`/api/carvermatrices/${matrixId}`, { responseType: "text" })
          .then((response) => {
            const dataString = response.data;
            let matrixData;
            try {
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

            if (!matrixData.items || !Array.isArray(matrixData.items)) {
              console.error("Matrix data does not contain a valid items array:", matrixData);
              return;
            }

            // Save raw items into provider (we'll let the UI decide which ones to display)
            setRawItems(matrixData.items);

            // Set the config, including hosts, participants, and the current user's email.
            setConfig({
              r2Multi: matrixData.r2Multi,
              randomAssignment: matrixData.randomAssignment,
              roleBased: matrixData.roleBased,
              fivePointScoring: matrixData.fivePointScoring,
              cMulti: matrixData.cMulti,
              aMulti: matrixData.aMulti,
              rMulti: matrixData.rMulti,
              vMulti: matrixData.vMulti,
              eMulti: matrixData.eMulti,
              description: matrixData.description,
              name: matrixData.name,
              hosts: matrixData.hosts || [],
              participants: matrixData.participants || [],
              currentUserEmail: currentUserEmail,
            });

            // For backwards compatibility (when roleBased is false), apply filtering based on randomAssignment.
            let processedItems = matrixData.items;
            if (!matrixData.roleBased) {
              processedItems =
                matrixData.randomAssignment === false
                  ? matrixData.items
                  : matrixData.items.filter((item: any) => {
                      if (Array.isArray(item.targetUsers)) {
                        return item.targetUsers.includes(currentUserEmail);
                      }
                      return false;
                    });
            }

            // Build a matrix map and id map from the processed items
            const matrixMap = new Map<string, Map<string, number>>();
            const idMap = new Map<string, number>();

            processedItems.forEach((item: any) => {
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
              idMap.set(item.itemName, item.itemId);
            });

            setMultiMatrix(matrixMap);
            setItemIdMap(idMap);
          })
          .catch((error) => {
            console.error("Error fetching matrix data:", error);
          });
      })
      .catch((error) => {
        console.error("Error fetching user info:", error);
      });
  }, [setMultiMatrix, setConfig, setItemIdMap, setRawItems]);

  return null;
};

export default MatrixLoader;

