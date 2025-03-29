import React, { useState, useMemo, useEffect } from "react";
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Tooltip,
  Snackbar,
  Alert,
  Tabs,
  Tab,
  Grid,
  LinearProgress,
  Chip,
} from "@mui/material";
import MatrixExplorer from "../components/custom/search/matrixExplorer";
import CategoryGroup from "../components/custom/editMatrix/categoryGroup";
import {
  MultiMatrixProvider,
  useMultiMatrix,
} from "../components/custom/multiMatrixProvider";
import MatrixLoader from "../components/custom/matrixLoader";
import { ExportPdfButton } from "../components/custom/pdfExport/ExportPdfButton";
import axios from "axios";
import SaveIcon from '@mui/icons-material/Save';
import PersonIcon from '@mui/icons-material/Person';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import GroupsIcon from '@mui/icons-material/Groups';
import SettingsIcon from '@mui/icons-material/Settings';

// Host Pane Component
const HostPane: React.FC<{
  config: any;
  items: any[];
  categories: string[];
}> = ({ config, items, categories }) => {
  // Add category mapping for correct property access
  const categoryToPropertyMap: { [key: string]: string } = {
    "Criticality": "criticality",
    "Accessibility": "accessibility",
    "Recuperability": "recoverability",
    "Vulnerability": "vulnerability",
    "Effect": "effect",
    "Recognizability": "recognizability",
  };

  // Get multipliers mapping
  const categoryToMultiplierMap: { [key: string]: string } = {
    "Criticality": "cMulti",
    "Accessibility": "aMulti",
    "Recuperability": "rMulti",
    "Vulnerability": "vMulti",
    "Effect": "eMulti",
    "Recognizability": "r2Multi",
  };

  // Calculate completion percentage for each category
  const categoryCompletions = useMemo(() => {
    const completions: { [key: string]: number } = {};
    
    categories.forEach(category => {
      const key = categoryToPropertyMap[category];
      const totalItems = items.length;
      let filledItems = 0;

      if (config.randomAssignment) {
        // For random assignment: check if all assigned users have submitted scores
        items.forEach(item => {
          const scores = item[key] || {};
          const targetUsers = item.targetUsers || [];
          if (targetUsers.length === 0) return; // Skip if no users assigned
          
          // Count how many assigned users have submitted a score
          const submittedUsers = targetUsers.filter((user: string) => 
            scores[user] !== undefined && scores[user] > 0
          );
          
          if (submittedUsers.length === targetUsers.length) {
            filledItems++;
          }
        });
      } else {
        // For non-random: check if all participants have submitted scores
        const participants = config.participants || [];
        if (participants.length === 0) return;

        items.forEach(item => {
          const scores = item[key] || {};
          const submittedUsers = participants.filter((user: string) => 
            scores[user] !== undefined && scores[user] > 0
          );
          
          if (submittedUsers.length === participants.length) {
            filledItems++;
          }
        });
      }

      completions[category] = totalItems > 0 ? (filledItems / totalItems) * 100 : 0;
    });
    
    return completions;
  }, [items, categories, config.randomAssignment, config.participants, categoryToPropertyMap]);

  // Calculate overall completion
  const overallCompletion = useMemo(() => {
    const total = Object.values(categoryCompletions).reduce((acc, val) => acc + val, 0);
    return total / categories.length;
  }, [categoryCompletions, categories]);

  // Calculate average scores with multipliers for the matrix view
  const getAverageScore = (item: any, category: string): number => {
    const key = categoryToPropertyMap[category];
    const scores = item[key] || {};
    const values = Object.values(scores) as number[];
    
    if (values.length === 0) return 0;
    
    const average = values.reduce((sum, score) => sum + score, 0) / values.length;
    const multiplier = config[categoryToMultiplierMap[category]] || 1;
    
    return Number((average * multiplier).toFixed(1));
  };

  return (
    <Box sx={{ p: 2 }}>
      <Grid container spacing={3}>
        {/* Overall Progress */}
        <Grid item xs={12}>
          <Paper
            sx={{
              p: 2,
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
            }}
          >
            <Typography variant="h6" sx={{ color: '#fff', mb: 1 }}>Overall Progress</Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
              <LinearProgress
                variant="determinate"
                value={overallCompletion}
                sx={{
                  flex: 1,
                  height: 10,
                  borderRadius: 5,
                  backgroundColor: 'rgba(255, 255, 255, 0.1)',
                  '& .MuiLinearProgress-bar': {
                    backgroundColor: '#014093',
                  },
                }}
              />
              <Typography sx={{ color: '#fff', minWidth: 45 }}>
                {Math.round(overallCompletion)}%
              </Typography>
            </Box>
          </Paper>
        </Grid>

        {/* Matrix Configuration */}
        <Grid item xs={12} md={6}>
          <Paper
            sx={{
              p: 2,
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <SettingsIcon sx={{ color: '#fff' }} />
              <Typography variant="h6" sx={{ color: '#fff' }}>Matrix Configuration</Typography>
            </Box>
            <Grid container spacing={2} sx={{ flex: 1, alignContent: 'flex-start' }}>
              <Grid item xs={6}>
                <Typography sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>Random Assignment</Typography>
                <Chip
                  label={config.randomAssignment ? "Enabled" : "Disabled"}
                  size="small"
                  sx={{
                    bgcolor: config.randomAssignment ? 'rgba(0, 230, 118, 0.1)' : 'rgba(255, 255, 255, 0.1)',
                    color: config.randomAssignment ? '#00E676' : '#fff',
                  }}
                />
              </Grid>
              <Grid item xs={6}>
                <Typography sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>Role Based</Typography>
                <Chip
                  label={config.roleBased ? "Enabled" : "Disabled"}
                  size="small"
                  sx={{
                    bgcolor: config.roleBased ? 'rgba(0, 230, 118, 0.1)' : 'rgba(255, 255, 255, 0.1)',
                    color: config.roleBased ? '#00E676' : '#fff',
                  }}
                />
              </Grid>
              <Grid item xs={6}>
                <Typography sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>Scoring Type</Typography>
                <Chip
                  label={config.fivePointScoring ? "5-Point" : "10-Point"}
                  size="small"
                  sx={{
                    bgcolor: 'rgba(255, 255, 255, 0.1)',
                    color: '#fff',
                  }}
                />
              </Grid>
              <Grid item xs={6}>
                <Typography sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>Total Targets</Typography>
                <Chip
                  label={items.length}
                  size="small"
                  sx={{
                    bgcolor: 'rgba(255, 255, 255, 0.1)',
                    color: '#fff',
                  }}
                />
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* Category Progress */}
        <Grid item xs={12} md={6}>
          <Paper
            sx={{
              p: 2,
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              height: '100%',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <GroupsIcon sx={{ color: '#fff' }} />
              <Typography variant="h6" sx={{ color: '#fff' }}>Category Progress</Typography>
            </Box>
            {categories.map((category) => (
              <Box key={category} sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>
                    {category.charAt(0)} ({config[categoryToMultiplierMap[category]]}x)
                  </Typography>
                  <Typography sx={{ color: '#fff' }}>
                    {Math.round(categoryCompletions[category])}%
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={categoryCompletions[category]}
                  sx={{
                    height: 6,
                    borderRadius: 3,
                    backgroundColor: 'rgba(255, 255, 255, 0.1)',
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: '#014093',
                    },
                  }}
                />
              </Box>
            ))}
          </Paper>
        </Grid>

        {/* Matrix Overview */}
        <Grid item xs={12} sx={{ mt: 4 }}>
          <Paper
            sx={{
              p: 2,
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AdminPanelSettingsIcon sx={{ color: '#fff' }} />
                <Typography variant="h6" sx={{ color: '#fff' }}>Matrix Overview</Typography>
              </Box>
              <Box
                sx={{
                  backgroundColor: 'rgba(147, 51, 234, 0.1)',
                  borderRadius: 2,
                  padding: '8px 24px',
                  transition: 'all 0.2s ease-in-out',
                  border: '1px solid rgba(147, 51, 234, 0.2)',
                  '&:hover': {
                    backgroundColor: 'rgba(147, 51, 234, 0.2)',
                    transform: 'translateY(-1px)',
                    boxShadow: '0 4px 8px rgba(147, 51, 234, 0.15)',
                    border: '1px solid rgba(147, 51, 234, 0.3)',
                  },
                }}
              >
                <ExportPdfButton config={config} items={items} />
              </Box>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell
                      sx={{
                        backgroundColor: '#014093',
                        color: '#fff',
                        fontWeight: 'bold',
                      }}
                    >
                      Target
                    </TableCell>
                    {categories.map((category) => (
                      <TableCell
                        key={category}
                        align="center"
                        sx={{
                          backgroundColor: '#014093',
                          color: '#fff',
                          fontWeight: 'bold',
                          width: '60px',
                        }}
                      >
                        {category.charAt(0)}
                      </TableCell>
                    ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {items.map((item) => (
                    <TableRow key={item.itemId}>
                      <TableCell
                        sx={{
                          color: '#fff',
                          fontWeight: 'bold',
                        }}
                      >
                        {item.itemName}
                      </TableCell>
                      {categories.map((category) => {
                        const avgScore = getAverageScore(item, category);
                        return (
                          <TableCell
                            key={category}
                            align="center"
                            sx={{
                              color: avgScore > 0 ? '#fff' : 'rgba(255, 255, 255, 0.3)',
                            }}
                          >
                            {avgScore}
                          </TableCell>
                        );
                      })}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

const EditMatrixContent: React.FC = () => {
  const { config, rawItems, updates } = useMultiMatrix();
  const currentEmail = config.currentUserEmail;
  const [successToast, setSuccessToast] = useState(false);
  const [errorToast, setErrorToast] = useState(false);
  
  const categoriesConst = [
    "Criticality",
    "Accessibility",
    "Recuperability",
    "Vulnerability",
    "Effect",
    "Recognizability",
  ] as const;

  const categories = [...categoriesConst];

  const categoryToPropertyMap: { [key: string]: string } = {
    "Criticality": "criticality",
    "Accessibility": "accessibility",
    "Recuperability": "recoverability",
    "Vulnerability": "vulnerability",
    "Effect": "effect",
    "Recognizability": "recognizability",
  };

  // Determine user roles if roleBased is enabled.
  const isRoleBased = config.roleBased;
  const isHost =
    isRoleBased && currentEmail ? config.hosts?.includes(currentEmail) : false;
  const isParticipant =
    isRoleBased && currentEmail ? config.participants?.includes(currentEmail) : false;

  const [activeView, setActiveView] = useState<'host' | 'participant'>(() => {
    // Set initial view based on user role
    if (isRoleBased && currentEmail) {
      if (isHost && !isParticipant) return 'host';
      if (isParticipant && !isHost) return 'participant';
      return 'participant'; // Default for users with both roles
    }
    return 'participant'; // Default for non-role-based matrices
  });

  // Force view to host if user is only a host
  useEffect(() => {
    if (isRoleBased && isHost && !isParticipant) {
      setActiveView('host');
    }
  }, [isRoleBased, isHost, isParticipant]);

  // Compute displayed items based on role
  const displayedItems = useMemo(() => {
    if (!currentEmail) return rawItems;

    if (isRoleBased) {
      if (isHost && !isParticipant) {
        // Host only: show all.
        return rawItems;
      } else if (isParticipant && !isHost) {
        // Participant only: show items based on assignment type
        if (config.randomAssignment) {
          // For random assignment, use targetUsers
          return rawItems.filter(
            (item: any) =>
              Array.isArray(item.targetUsers) && item.targetUsers.includes(currentEmail)
          );
        } else {
          // For non-random assignment, show all items if user is in participants list
          return config.participants?.includes(currentEmail) ? rawItems : [];
        }
      } else if (isHost && isParticipant) {
        // Both: show participant view in participant mode, all items in host mode
        if (activeView === 'participant') {
          if (config.randomAssignment) {
            // For random assignment, use targetUsers
            return rawItems.filter(
              (item: any) =>
                Array.isArray(item.targetUsers) && item.targetUsers.includes(currentEmail)
            );
          } else {
            // For non-random assignment, show all items if user is in participants list
            return config.participants?.includes(currentEmail) ? rawItems : [];
          }
        } else {
          return rawItems;
        }
      }
    }
    // Not roleBased: show all items
    return rawItems;
  }, [isRoleBased, isHost, isParticipant, rawItems, currentEmail, activeView, config.randomAssignment, config.participants]);

  // Debugging: log when displayedItems changes.
  useEffect(() => {
    console.log("Displayed items count:", displayedItems.length);
  }, [displayedItems]);

  // Build matrix map and sorted target names.
  const matrixMap = useMemo(() => {
    const map = new Map<string, Map<string, number>>();
    displayedItems.forEach((item: any) => {
      const categoryMap = new Map<string, number>();
      categories.forEach(category => {
        const key = categoryToPropertyMap[category];
        // Get the user's score from the scores object for this category
        const scores = (item[key] || {}) as { [email: string]: number };
        const userScore = currentEmail ? (scores[currentEmail] || 0) : 0;
        categoryMap.set(category, userScore);
      });
      map.set(item.itemName, categoryMap);
    });
    return map;
  }, [displayedItems, categories, currentEmail, categoryToPropertyMap]);

  const targets = useMemo(() => {
    return Array.from(matrixMap.keys()).sort((a, b) => a.localeCompare(b));
  }, [matrixMap]);

  const handleSubmitUpdates = () => {
    const params = new URLSearchParams(window.location.search);
    const matrixId = params.get("matrixId");
    if (!matrixId) {
      console.error("matrixId query parameter is missing.");
      setErrorToast(true);
      return;
    }

    // Format updates to send only the current user's scores
    const formattedUpdates = updates.map((update: any) => {
      const formattedUpdate: any = { itemId: update.itemId };
      
      // For each category in the update
      Object.entries(update).forEach(([key, value]) => {
        if (key !== 'itemId') {
          // If the value is an object with user scores, get current user's score
          const scores = value as { [email: string]: number };
          formattedUpdate[key] = scores[currentEmail || ''] || 0;
        }
      });
      
      return formattedUpdate;
    });

    console.log("Sending formatted updates:", formattedUpdates);

    axios
      .put(`/api/carvermatrices/${matrixId}/carveritems/update`, formattedUpdates)
      .then((response) => {
        console.log("Updates submitted successfully", response);
        setSuccessToast(true);
      })
      .catch((error) => {
        console.error("Error submitting updates", error);
        setErrorToast(true);
      });
  };

  const handleViewChange = (_: React.SyntheticEvent, newValue: 'host' | 'participant') => {
    setActiveView(newValue);
  };

  return (
    <Box 
      sx={{ 
        height: "calc(100vh - 64px - 48px)", // Subtract navbar height (64px) and total margins (24px top + 24px bottom)
        backgroundColor: "#1a1a1a",
        color: "#ffffff",
        position: "relative",
        p: 3,
        display: 'flex',
        flexDirection: 'column',
        gap: 3,
        overflow: 'hidden', // Prevent content from overflowing
        width: '100%',
        boxSizing: 'border-box',
      }}
    >
      {/* Background Pattern */}
      <Box
        sx={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundImage: "linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.7)), url('/military-pattern.svg')",
          backgroundSize: "100px 100px",
          backgroundPosition: "center",
          opacity: 0.1,
          zIndex: 0,
        }}
      />

      <MatrixLoader />
      
      {/* Success Toast */}
      <Snackbar
        open={successToast}
        autoHideDuration={3000}
        onClose={() => setSuccessToast(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setSuccessToast(false)}
          severity="success"
          variant="filled"
          sx={{
            backgroundColor: '#1E4620',
            color: '#ffffff',
            '& .MuiAlert-icon': {
              color: '#ffffff'
            },
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            backdropFilter: 'blur(10px)',
          }}
        >
          Matrix updated successfully
        </Alert>
      </Snackbar>

      {/* Error Toast */}
      <Snackbar
        open={errorToast}
        autoHideDuration={4000}
        onClose={() => setErrorToast(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setErrorToast(false)}
          severity="error"
          variant="filled"
          sx={{
            backgroundColor: '#7C0B02',
            color: '#ffffff',
            '& .MuiAlert-icon': {
              color: '#ffffff'
            },
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            backdropFilter: 'blur(10px)',
          }}
        >
          Failed to update matrix. Please try again.
        </Alert>
      </Snackbar>

      <Box 
        sx={{ 
          display: "flex", 
          gap: 3,
          position: "relative",
          zIndex: 1,
          flex: 1,
          minHeight: 0,
          overflow: 'hidden',
          width: '100%',
          boxSizing: 'border-box',
        }}
      >
        {/* Matrix Explorer Sidebar */}
        <Paper
          sx={{
            width: "300px",
            backgroundColor: "rgba(255, 255, 255, 0.05)",
            backdropFilter: "blur(10px)",
            border: "1px solid rgba(255, 255, 255, 0.1)",
            overflowY: "auto",
            flexShrink: 0,
            borderRadius: 2,
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <MatrixExplorer />
        </Paper>

        {/* Main Content Area */}
        <Box 
          sx={{ 
            flex: 1,
            display: "flex",
            flexDirection: "column",
            minHeight: 0,
            overflow: 'hidden',
            minWidth: 0, // Add this to prevent flex item from overflowing
          }}
        >
          {/* Header */}
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2, flexShrink: 0 }}>
            <Typography
              variant="h5"
              sx={{
                color: "#ffffff",
                fontWeight: "bold",
                textTransform: "uppercase",
                letterSpacing: "1px",
                fontFamily: "'Roboto Condensed', sans-serif",
              }}
            >
              {config.name || "Matrix Editor"}
            </Typography>
            <Box sx={{ display: "flex", gap: 1, alignItems: 'center' }}>
              <Tooltip title="Save Changes">
                <IconButton
                  onClick={handleSubmitUpdates}
                  sx={{
                    color: '#00E676',
                    '&:hover': {
                      backgroundColor: 'rgba(0, 230, 118, 0.1)',
                    },
                  }}
                >
                  <SaveIcon />
                </IconButton>
              </Tooltip>
              {(!isRoleBased || (isHost && isParticipant)) && (
                <Paper
                  sx={{
                    backgroundColor: 'rgba(255, 255, 255, 0.05)',
                    backdropFilter: 'blur(10px)',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                  }}
                >
                  <Tabs
                    value={activeView}
                    onChange={handleViewChange}
                    sx={{
                      minHeight: 40,
                      '& .MuiTab-root': {
                        minHeight: 40,
                        color: 'rgba(255, 255, 255, 0.7)',
                      },
                      '& .Mui-selected': {
                        color: '#fff !important',
                      },
                      '& .MuiTabs-indicator': {
                        backgroundColor: '#014093',
                      },
                    }}
                  >
                    {(!isRoleBased || isParticipant) && (
                      <Tab
                        icon={<PersonIcon sx={{ fontSize: 20 }} />}
                        iconPosition="start"
                        label="Participant"
                        value="participant"
                        sx={{ fontSize: 14 }}
                      />
                    )}
                    {(!isRoleBased || isHost) && (
                      <Tab
                        icon={<AdminPanelSettingsIcon sx={{ fontSize: 20 }} />}
                        iconPosition="start"
                        label="Host"
                        value="host"
                        sx={{ fontSize: 14 }}
                      />
                    )}
                  </Tabs>
                </Paper>
              )}
            </Box>
          </Box>

          {/* Content Area */}
          <Box sx={{ 
            flex: 1,
            display: 'flex',
            overflow: 'hidden',
            position: 'relative',
          }}>
            {(!isRoleBased || isHost) && activeView === 'host' ? (
              <Box sx={{ 
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                overflow: 'auto',
                '&::-webkit-scrollbar': {
                  width: '12px',
                },
                '&::-webkit-scrollbar-track': {
                  background: 'rgba(255, 255, 255, 0.05)',
                  borderRadius: '6px',
                },
                '&::-webkit-scrollbar-thumb': {
                  backgroundColor: 'rgba(255, 255, 255, 0.1)',
                  borderRadius: '6px',
                  border: '3px solid rgba(0, 0, 0, 0)',
                  backgroundClip: 'padding-box',
                  '&:hover': {
                    backgroundColor: 'rgba(255, 255, 255, 0.2)',
                  },
                },
                scrollbarWidth: 'thin',
                scrollbarColor: 'rgba(255, 255, 255, 0.1) rgba(255, 255, 255, 0.05)',
              }}>
                <HostPane config={config} items={rawItems} categories={categories} />
              </Box>
            ) : (
              <TableContainer
                component={Paper}
                sx={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  backgroundColor: "rgba(255, 255, 255, 0.05)",
                  backdropFilter: "blur(10px)",
                  border: "1px solid rgba(255, 255, 255, 0.1)",
                  borderRadius: 2,
                  overflowY: 'auto',
                  overflowX: 'hidden',
                  '&::-webkit-scrollbar': {
                    width: '12px',
                  },
                  '&::-webkit-scrollbar-track': {
                    background: 'rgba(255, 255, 255, 0.05)',
                    borderRadius: '6px',
                  },
                  '&::-webkit-scrollbar-thumb': {
                    backgroundColor: 'rgba(255, 255, 255, 0.1)',
                    borderRadius: '6px',
                    border: '3px solid rgba(0, 0, 0, 0)',
                    backgroundClip: 'padding-box',
                    '&:hover': {
                      backgroundColor: 'rgba(255, 255, 255, 0.2)',
                    },
                  },
                  scrollbarWidth: 'thin',
                  scrollbarColor: 'rgba(255, 255, 255, 0.1) rgba(255, 255, 255, 0.05)',
                }}
              >
                <Table 
                  stickyHeader 
                  sx={{ 
                    tableLayout: 'fixed',
                    width: '100%',
                  }}
                >
                  <TableHead>
                    <TableRow>
                      <TableCell
                        sx={{
                          backgroundColor: "#014093",
                          color: "#ffffff",
                          fontWeight: "bold",
                          fontFamily: "'Roboto Condensed', sans-serif",
                          textTransform: "uppercase",
                          letterSpacing: "0.5px",
                          width: '25%',
                        }}
                      >
                        Target
                      </TableCell>
                      {categories.map((category) => (
                        <TableCell
                          key={category}
                          align="center"
                          sx={{
                            backgroundColor: "#014093",
                            color: "#ffffff",
                            fontWeight: "bold",
                            fontFamily: "'Roboto Condensed', sans-serif",
                            textTransform: "uppercase",
                            letterSpacing: "0.5px",
                          }}
                        >
                          {category.charAt(0)}
                        </TableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {targets.map((target) => (
                      <TableRow key={target}>
                        <TableCell
                          sx={{
                            color: "#ffffff",
                            fontWeight: "bold",
                            fontFamily: "'Roboto Condensed', sans-serif",
                          }}
                        >
                          {target}
                        </TableCell>
                        {categories.map((category) => {
                          const item = displayedItems.find(item => item.itemName === target);
                          const key = categoryToPropertyMap[category];
                          const scores = item ? (item[key] || {}) as { [email: string]: number } : {};
                          const userScore = currentEmail ? (scores[currentEmail] || 0) : 0;
                          
                          return (
                            <TableCell
                              key={category}
                              align="center"
                              sx={{
                                color: userScore > 0 ? '#fff' : 'rgba(255, 255, 255, 0.3)',
                              }}
                            >
                              <CategoryGroup
                                category={category}
                                targetTitle={target}
                              />
                            </TableCell>
                          );
                        })}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Box>
        </Box>
      </Box>
    </Box>
  );
};

export const EditMatrix: React.FC = () => {
  return (
    <MultiMatrixProvider>
      <EditMatrixContent />
    </MultiMatrixProvider>
  );
};

export default EditMatrix;

