import React from "react";
import { Box, FormControl, Select, MenuItem, SelectChangeEvent, Tooltip } from "@mui/material";
import { useMultiMatrix } from "../multiMatrixProvider";

interface CategoryDisplayProps {
  category: string;
  targetTitle: string;
}

const CategoryGroup: React.FC<CategoryDisplayProps> = ({ category, targetTitle }) => {
  const { setMultiMatrix, itemIdMap, setUpdates, rawItems, config } = useMultiMatrix();
  const currentEmail = config.currentUserEmail;

  // Map the UI category names to API field names.
  const categoryMap: { [key: string]: string } = {
    "Criticality": "criticality",
    "Accessibility": "accessibility",
    "Recuperability": "recoverability",
    "Vulnerability": "vulnerability",
    "Effect": "effect",
    "Recognizability": "recognizability",
  };

  // Score descriptions for 5-point scale
  const fivePointDescriptions: { [key: string]: { [score: number]: string } } = {
    "Criticality": {
      1: "Minimal importance, negligible impact if compromised",
      2: "Low importance, limited impact on operations",
      3: "Moderate importance, noticeable impact on operations",
      4: "High importance, significant impact on operations",
      5: "Critical importance, severe impact if compromised"
    },
    "Accessibility": {
      1: "Extremely difficult to access, multiple strong barriers",
      2: "Difficult to access, several security measures",
      3: "Moderate accessibility, standard security measures",
      4: "Relatively easy to access, limited security",
      5: "Very easy to access, minimal security barriers"
    },
    "Recuperability": {
      1: "Quick recovery (hours), minimal resources needed",
      2: "Short recovery (days), modest resources needed",
      3: "Moderate recovery (weeks), significant resources",
      4: "Long recovery (months), extensive resources",
      5: "Extended recovery (years), massive resources"
    },
    "Vulnerability": {
      1: "Minimal vulnerabilities, highly secure",
      2: "Few vulnerabilities, well-protected",
      3: "Moderate vulnerabilities, standard protection",
      4: "Significant vulnerabilities, limited protection",
      5: "Highly vulnerable, minimal protection"
    },
    "Effect": {
      1: "Minimal effect, barely noticeable impact",
      2: "Minor effect, limited operational impact",
      3: "Moderate effect, noticeable disruption",
      4: "Major effect, significant disruption",
      5: "Severe effect, critical system failure"
    },
    "Recognizability": {
      1: "Very difficult to identify, requires expertise",
      2: "Difficult to identify, needs specific knowledge",
      3: "Moderately recognizable with training",
      4: "Easily recognizable with basic knowledge",
      5: "Highly visible and immediately recognizable"
    }
  };

  // Score descriptions for 10-point scale
  const tenPointDescriptions: { [key: string]: { [score: number]: string } } = {
    "Criticality": {
      1: "Negligible importance, no operational impact",
      2: "Very low importance, minimal effect on operations",
      3: "Low importance, slight operational impact",
      4: "Moderate-low importance, noticeable effects",
      5: "Moderate importance, clear operational impact",
      6: "Moderate-high importance, substantial effects",
      7: "High importance, major operational impact",
      8: "Very high importance, severe disruption likely",
      9: "Critical importance, potential catastrophic effects",
      10: "Highest criticality, devastating impact guaranteed"
    },
    "Accessibility": {
      1: "Virtually inaccessible, maximum security measures",
      2: "Extremely difficult access, multiple advanced barriers",
      3: "Very difficult access, sophisticated security",
      4: "Difficult access, multiple security layers",
      5: "Moderate access difficulty, standard security",
      6: "Somewhat accessible, basic security measures",
      7: "Relatively easy access, limited security",
      8: "Easy access, minimal security measures",
      9: "Very easy access, token security presence",
      10: "Completely accessible, no security barriers"
    },
    "Recuperability": {
      1: "Immediate recovery (hours), minimal resources",
      2: "Very quick recovery (1-2 days), basic resources",
      3: "Quick recovery (3-5 days), limited resources",
      4: "Short recovery (1-2 weeks), moderate resources",
      5: "Moderate recovery (2-4 weeks), significant resources",
      6: "Extended recovery (1-2 months), substantial resources",
      7: "Long recovery (2-6 months), major resources",
      8: "Very long recovery (6-12 months), extensive resources",
      9: "Prolonged recovery (1-2 years), massive resources",
      10: "Extreme recovery (2+ years), extraordinary resources"
    },
    "Vulnerability": {
      1: "Virtually invulnerable, exceptional protection",
      2: "Extremely secure, multiple advanced protections",
      3: "Very secure, sophisticated protection measures",
      4: "Well-protected, multiple security layers",
      5: "Moderately protected, standard security",
      6: "Somewhat vulnerable, basic protection",
      7: "Notably vulnerable, limited protection",
      8: "Highly vulnerable, minimal protection",
      9: "Extremely vulnerable, negligible protection",
      10: "Completely vulnerable, no protection"
    },
    "Effect": {
      1: "Negligible effect, no operational impact",
      2: "Very minor effect, barely noticeable",
      3: "Minor effect, limited local impact",
      4: "Moderate-low effect, noticeable disruption",
      5: "Moderate effect, clear operational impact",
      6: "Significant effect, substantial disruption",
      7: "Major effect, severe operational impact",
      8: "Severe effect, critical system damage",
      9: "Very severe effect, near-total failure",
      10: "Catastrophic effect, complete system collapse"
    },
    "Recognizability": {
      1: "Extremely difficult to identify, requires expert analysis",
      2: "Very difficult to identify, needs specialized knowledge",
      3: "Difficult to identify, requires specific training",
      4: "Somewhat difficult to identify, needs experience",
      5: "Moderately recognizable with proper training",
      6: "Fairly recognizable with basic knowledge",
      7: "Easily recognizable with minimal training",
      8: "Very easily recognizable, obvious to most",
      9: "Highly visible, immediately apparent",
      10: "Unmistakable, impossible to miss"
    }
  };

  const apiCategory = categoryMap[category] || category.toLowerCase();

  // Find the raw item for this target
  const rawItem = rawItems.find(item => item.itemName === targetTitle);
  const scores = rawItem ? (rawItem[apiCategory] || {}) as { [email: string]: number } : {};
  const initialScore = currentEmail ? scores[currentEmail] || 0 : 0;
  const [score, setScore] = React.useState<number>(initialScore);

  // Generate menu items based on scoring type
  const menuItems = React.useMemo(() => {
    const items = [<MenuItem key="0" value="0">-</MenuItem>];
    const maxScore = config.fivePointScoring ? 5 : 10;
    const descriptions = config.fivePointScoring ? fivePointDescriptions : tenPointDescriptions;
    
    for (let i = 1; i <= maxScore; i++) {
      const value = i.toString();
      items.push(
        <MenuItem key={i} value={value}>
          <Tooltip title={descriptions[category][i]} placement="right">
            <span>{i}</span>
          </Tooltip>
        </MenuItem>
      );
    }
    
    return items;
  }, [config.fivePointScoring, category]);

  const handleInputChange = (event: SelectChangeEvent<string>) => {
    if (!currentEmail) return;
    
    const newScore = Number(event.target.value);
    setScore(newScore);

    // Update the local matrix state.
    setMultiMatrix((prevMatrix) => {
      const updatedMatrix = new Map(prevMatrix);
      const targetMap = updatedMatrix.get(targetTitle)
        ? new Map(updatedMatrix.get(targetTitle))
        : new Map<string, number>();
      targetMap.set(category, newScore);
      updatedMatrix.set(targetTitle, targetMap);
      return updatedMatrix;
    });

    // Also update the "updates" state to record this change.
    const itemId = itemIdMap.get(targetTitle);
    if (!itemId) {
      console.warn(`No itemId found for ${targetTitle}`);
      return;
    }

    setUpdates((prevUpdates) => {
      const existingIndex = prevUpdates.findIndex((u: any) => u.itemId === itemId);
      if (existingIndex !== -1) {
        // Get existing scores or initialize new ones
        const existingScores = prevUpdates[existingIndex][apiCategory] || {};
        const updatedScores = {
          ...existingScores,
          [currentEmail]: newScore
        };

        // Merge the new change with the existing update
        const updatedItem = {
          ...prevUpdates[existingIndex],
          [apiCategory]: updatedScores
        };
        const newUpdates = [...prevUpdates];
        newUpdates[existingIndex] = updatedItem;
        return newUpdates;
      } else {
        // Create a new update entry with user-specific score
        return [...prevUpdates, {
          itemId,
          [apiCategory]: {
            [currentEmail]: newScore
          }
        }];
      }
    });
  };

  return (
    <Box display="flex" justifyContent="center" alignItems="center" sx={{ width: "100%" }}>
      <FormControl fullWidth sx={{ mt: 1, minWidth: 50 }}>
        <Select
          id="score-select"
          value={score.toString()}
          onChange={handleInputChange}
          sx={{
            ".MuiSelect-select": {
              color: "#ffffff",
              fontSize: { xs: "0.75rem", md: "1rem" },
              minWidth: 50,
            },
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: 'rgba(255, 255, 255, 0.23)',
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: '#014093',
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: '#014093',
            },
            '& .MuiSelect-icon': {
              color: 'rgba(255, 255, 255, 0.7)',
            },
          }}
          MenuProps={{
            PaperProps: {
              sx: {
                maxHeight: 300,
                backgroundColor: '#1a1a1a',
                color: '#ffffff',
                '& .MuiMenuItem-root': {
                  color: '#ffffff',
                  '&:hover': {
                    backgroundColor: 'rgba(1, 64, 147, 0.2)',
                  },
                },
              },
            },
          }}
        >
          {menuItems}
        </Select>
      </FormControl>
    </Box>
  );
};

export default CategoryGroup;

