import {
  Box,
  Button,
  FormControl,
  FormControlLabel,
  FormLabel,
  MenuItem,
  Paper,
  Select,
  SelectChangeEvent,
  Switch,
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TextField,
  Snackbar,
  Alert,
  Tooltip,
} from "@mui/material";
import { useState } from "react";
import { IconButton } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { whoamiUpsert, createMatrix } from "./apiService";
import SendIcon from "@mui/icons-material/Send";
import { useNavigate } from "react-router-dom";

// Tooltip descriptions to be added:
// - Matrix Parameters: ADD TOOLTIP DESCRIPTION HERE
// - Global Category Multipliers: ADD TOOLTIP DESCRIPTION HERE
// - Score Range: ADD TOOLTIP DESCRIPTION HERE
// - Role-Based Matrix: ADD TOOLTIP DESCRIPTION HERE
// - Data Entry Assignment Method: ADD TOOLTIP DESCRIPTION HERE
// - Targets: ADD TOOLTIP DESCRIPTION HERE
// - Collaborators: ADD TOOLTIP DESCRIPTION HERE

export const CreateMatrix: React.FC = () => {
  const [RoleBasedChecked, setRoleBasedChecked] = useState(true);
  const [value, setValue] = useState<number>(5);
  const [randomAssigned, setRandomAssigned] = useState<string>("random");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const navigate = useNavigate();
  const [targets, setTargets] = useState<string[]>([]);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarErrorOpen, setSnackbarErrorOpen] = useState(false);
  const [titleError, setTitleError] = useState(false);
  const [snackbarTargetErrorOpen, setSnackbarTargetErrorOpen] = useState(false);
  const [participantsData, setParticipantsData] = useState<
    { email: string; role: string }[]
  >([]);

  const initialMultipliers = {
    Criticality: 1.0,
    Vulnerability: 1.0,
    Accessibility: 1.0,
    Effect: 1.0,
    Recoverability: 1.0,
    Recognizability: 1.0,
  };

  const [multipliers, setMultipliers] = useState(initialMultipliers);

  const carverOrder = [
    { key: "Criticality", header: "C" },
    { key: "Accessibility", header: "A" },
    { key: "Recoverability", header: "R" },
    { key: "Vulnerability", header: "V" },
    { key: "Effect", header: "E" },
    { key: "Recognizability", header: "R" },
  ] as const;

  const carverTooltips: Record<typeof carverOrder[number]['key'], string> = {
    Criticality: "The primary measure of target value and importance. Higher values indicate greater significance to the system or organization.",
    Accessibility: "The ease of reaching and accessing the target. Higher values suggest easier access with fewer security measures.",
    Recoverability: "The time and resources needed to restore functionality after an incident. Higher values mean longer recovery times.",
    Vulnerability: "The susceptibility to attack or disruption. Higher values indicate greater weaknesses or vulnerabilities.",
    Effect: "The immediate impact of a successful attack. Higher values represent more severe immediate consequences.",
    Recognizability: "How easily the target can be identified. Higher values mean the target is more recognizable and requires less preparation to identify.",
  };

  // Participant management
  const handleAddParticipant = () => {
    setParticipantsData([
      ...participantsData,
      { email: "", role: "Participant" },
    ]);
  };

  const handleParticipantEmailChange = (
    index: number,
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const newParticipants = [...participantsData];
    newParticipants[index].email = event.target.value;
    setParticipantsData(newParticipants);
  };

  const handleParticipantRoleChange = (
    index: number,
    event: SelectChangeEvent<string>
  ) => {
    const newParticipants = [...participantsData];
    newParticipants[index].role = event.target.value;
    setParticipantsData(newParticipants);
  };

  const handleDeleteParticipant = (index: number) => {
    setParticipantsData(participantsData.filter((_, i) => i !== index));
  };

  // Target management
  const handleAddTarget = () => {
    setTargets([...targets, ""]);
  };

  const handleTargetChange = (
    index: number,
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const newTargets = [...targets];
    newTargets[index] = event.target.value;
    setTargets(newTargets);
  };

  const handleDeleteTarget = (index: number) => {
    setTargets(targets.filter((_, i) => i !== index));
  };

  // Dropdown options for multipliers
  const options = [
    0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9,
    2.0,
  ];

  const multipliersHandleChange =
    (label: string) => (event: SelectChangeEvent<unknown>) => {
      const val = event.target.value as number;
      setMultipliers((prev) => ({
        ...prev,
        [label]: val,
      }));
    };

  const roleBasedHandleToggle = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRoleBasedChecked(event.target.checked);
  };

  const scoreRangeHandleChange = (event: SelectChangeEvent<number>) => {
    setValue(event.target.value as number);
  };

  const dataEntryHandleChange = (event: SelectChangeEvent<string>) => {
    setRandomAssigned(event.target.value as string);
  };

  const handleCreateMatrix = async () => {
    try {
      // Duplicate check for targets
      const trimmedTargets = targets.map((target) => target.trim());
      const uniqueTargets = new Set(trimmedTargets);
      if (uniqueTargets.size !== trimmedTargets.length) {
        setSnackbarErrorOpen(true);
        return;
      }

      if (!title.trim()) {
        setTitleError(true);
        return;
      } else {
        setTitleError(false);
      }

      const whoamiUpsertResponse = await whoamiUpsert();
      const rawData = whoamiUpsertResponse.data;
      const firstObjectEnd = rawData.indexOf("}") + 1;
      const firstObjectStr = rawData.substring(0, firstObjectEnd);
      const parsedFirstObject = JSON.parse(firstObjectStr);
      const { userId, email } = parsedFirstObject;

      const items = targets.map((target) => ({
        itemName: target,
        criticality: 0,
        accessibility: 0,
        recoverability: 0,
        vulnerability: 0,
        effect: 0,
        recognizability: 0,
      }));

      const hosts = [
        email,
        ...participantsData
          .map((p) =>
            p.role === "Host" || p.role === "Host and Participant" ? p.email : null
          )
          .filter((email): email is string => email !== null),
      ];

      const participantEmails = participantsData
        .map((p) =>
          p.role === "Participant" || p.role === "Host and Participant" ? p.email : null
        )
        .filter((email): email is string => email !== null);

      const matrixData = {
        name: title,
        description: description,
        hosts: hosts,
        participants: participantEmails,
        cMulti: multipliers["Criticality"],
        aMulti: multipliers["Accessibility"],
        rMulti: multipliers["Recoverability"],
        vMulti: multipliers["Vulnerability"],
        eMulti: multipliers["Effect"],
        r2Multi: multipliers["Recognizability"],
        randomAssignment: randomAssigned === "random" ? true : false,
        roleBased: RoleBasedChecked,
        fivePointScoring: value === 5 ? true : false,
        items: items,
      };
      if (targets.length != 0)
      {
        const response = await createMatrix(matrixData, userId);
        console.log("Matrix Created:", response.data);
        setSnackbarOpen(true);
        setTimeout(() => {
          navigate("/ViewMatrix");
        }, 3000);
      }
      else
      {
        setSnackbarOpen(false);
        setSnackbarTargetErrorOpen(true);
      }
    } catch (error) {
      console.error("Error creating matrix or verifying user", error);
    }
  };

  return (
    <Box
      sx={{
        backgroundColor: "#1a1a1a",
        height: "100vh",
        width: "100vw",
        display: "flex",
        justifyContent: "space-around",
        alignItems: "flex-start",
        pt: 1,
        position: "relative",
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

      {/* Left section: Matrix parameters and multipliers */}
      <Box
        id="matrixCreatorBox"
        sx={{
          backgroundColor: "rgba(255, 255, 255, 0.05)",
          backdropFilter: "blur(10px)",
          border: "1px solid rgba(255, 255, 255, 0.1)",
          p: 2,
          borderRadius: 2,
          width: "48%",
          height: "85%",
          maxHeight: "85%",
          display: "flex",
          flexDirection: "column",
          overflow: "auto",
          position: "relative",
          zIndex: 1,
        }}
      >
        <Box display="flex" alignItems="center" gap={2}>
          <TextField
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            variant="standard"
            placeholder="Enter Matrix Title..."
            InputProps={{
              disableUnderline: false,
              style: { fontSize: "3rem", color: "#ffffff" },
            }}
            sx={{ 
              flexGrow: 1,
              '& .MuiInput-root': {
                color: '#ffffff',
                '&:before': {
                  borderBottomColor: 'rgba(255, 255, 255, 0.23)',
                },
                '&:hover:before': {
                  borderBottomColor: '#014093',
                },
                '&.Mui-focused:before': {
                  borderBottomColor: '#014093',
                },
              },
            }}
            error={titleError}
            helperText={titleError ? "Matrix title is required" : ""}
          />
          <Button
            variant="contained"
            onClick={handleCreateMatrix}
            endIcon={<SendIcon />}
            sx={{ 
              borderRadius: "20px", 
              width: "170px",
              backgroundColor: "#014093",
              color: "#ffffff",
              textTransform: "uppercase",
              fontWeight: "bold",
              letterSpacing: "1px",
              flexShrink: 0,
              '&:hover': {
                backgroundColor: "#012B61",
              },
            }}
          >
            Create Matrix
          </Button>
        </Box>

        <Snackbar
          open={snackbarTargetErrorOpen}
          autoHideDuration={3000}
          onClose={() => setSnackbarTargetErrorOpen(false)}
        >
          <Alert
            onClose={() => setSnackbarTargetErrorOpen(false)}
            severity="error"
            variant="filled"
            sx={{ 
              width: "100%",
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
            Must have at least one target
          </Alert>
      </Snackbar>


        <Snackbar
          open={snackbarOpen}
          autoHideDuration={3000}
          onClose={() => setSnackbarOpen(false)}
        >
          <Alert
            onClose={() => setSnackbarOpen(false)}
            severity="success"
            variant="filled"
            sx={{ 
              width: "100%",
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
            Matrix Created Successfully! Redirecting to 'View Matrix' page
          </Alert>
        </Snackbar>

        <Snackbar
          open={snackbarErrorOpen}
          autoHideDuration={3000}
          onClose={() => setSnackbarErrorOpen(false)}
        >
          <Alert
            onClose={() => setSnackbarErrorOpen(false)}
            severity="error"
            variant="filled"
            sx={{ 
              width: "100%",
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
            You cannot have duplicate target names
          </Alert>
        </Snackbar>

        <TextField
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          multiline
          rows={1}
          variant="outlined"
          fullWidth
          placeholder="Enter Matrix description..."
          sx={{ 
            mt: 2,
            '& .MuiOutlinedInput-root': {
              color: '#ffffff',
              '& fieldset': {
                borderColor: 'rgba(255, 255, 255, 0.23)',
              },
              '&:hover fieldset': {
                borderColor: '#014093',
              },
              '&.Mui-focused fieldset': {
                borderColor: '#014093',
              },
            },
            '& .MuiInputBase-input::placeholder': {
              color: 'rgba(255, 255, 255, 0.5)',
            },
          }}
        />

        <Box
          id="matrixParametersBox"
          sx={{
            width: "100%",
            mt: 3,
            display: "flex",
            flexDirection: "column",
            marginTop: "50px",
          }}
        >
          <Tooltip title="" placement="right">
            <Typography 
              variant="h4" 
              sx={{ 
                color: "#ffffff",
                fontWeight: "bold",
                textTransform: "uppercase",
                letterSpacing: "1px",
                fontFamily: "'Roboto Condensed', sans-serif",
                mb: 2,
                cursor: "help",
              }}
            >
              Matrix Parameters
            </Typography>
          </Tooltip>
          <Box
            sx={{
              display: "flex",
              flexDirection: "row",
              gap: 4,
            }}
          >
            <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
              <FormControl fullWidth>
                <Tooltip title="" placement="top">
                  <FormLabel 
                    id="score-range-label"
                    sx={{ color: "rgba(255, 255, 255, 0.7)", cursor: "help" }}
                  >
                    Score Range
                  </FormLabel>
                </Tooltip>
                <Select
                  labelId="score-range-label"
                  value={value}
                  onChange={scoreRangeHandleChange}
                  sx={{
                    "& .MuiInputBase-input": { color: "#ffffff" },
                    "& .MuiSelect-icon": { color: "#ffffff" },
                    border: "1px solid rgba(255, 255, 255, 0.23)",
                    borderRadius: "20px",
                    mb: 1,
                    backgroundColor: "rgba(255, 255, 255, 0.05)",
                    '&:hover': {
                      borderColor: '#014093',
                    },
                    '&.Mui-focused': {
                      borderColor: '#014093',
                    },
                  }}
                >
                  <MenuItem value={5}>5-Point Scoring</MenuItem>
                  <MenuItem value={10}>10-Point Scoring</MenuItem>
                </Select>
              </FormControl>
              <Tooltip title="" placement="top">
                <Typography sx={{ color: "rgba(255, 255, 255, 0.7)", cursor: "help" }}>Enforce Role Restrictions</Typography>
              </Tooltip>
              <FormControlLabel
                control={
                  <Switch
                    checked={RoleBasedChecked}
                    onChange={roleBasedHandleToggle}
                    sx={{
                      "& .MuiSwitch-track": {
                        borderRadius: "20px",
                        backgroundColor: RoleBasedChecked ? "#014093" : "rgba(255, 255, 255, 0.23)",
                        opacity: 1,
                      },
                      "& .MuiSwitch-thumb": {
                        backgroundColor: "#ffffff",
                      },
                    }}
                  />
                }
                label={
                  <Typography sx={{ color: "rgba(255, 255, 255, 0.7)" }}>
                    {RoleBasedChecked ? "Enabled" : "Disabled"}
                  </Typography>
                }
                sx={{ mb: 1 }}
              />
            </Box>
            <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
              <FormControl fullWidth>
                <Tooltip title="" placement="top">
                  <FormLabel 
                    id="data-entry-label"
                    sx={{ color: "rgba(255, 255, 255, 0.7)", cursor: "help" }}
                  >
                    Data Entry Assignment Method
                  </FormLabel>
                </Tooltip>
                <Select
                  labelId="data-entry-label"
                  value={randomAssigned}
                  onChange={dataEntryHandleChange}
                  sx={{
                    "& .MuiInputBase-input": { color: "#ffffff" },
                    "& .MuiSelect-icon": { color: "#ffffff" },
                    border: "1px solid rgba(255, 255, 255, 0.23)",
                    borderRadius: "20px",
                    mb: 1,
                    backgroundColor: "rgba(255, 255, 255, 0.05)",
                    '&:hover': {
                      borderColor: '#014093',
                    },
                    '&.Mui-focused': {
                      borderColor: '#014093',
                    },
                  }}
                >
                  <MenuItem value={"random"}>Random</MenuItem>
                  <MenuItem value={"assigned"}>Assigned</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </Box>

          <Box
            id="GlobalCategoryMultipliersBox"
            sx={{
              width: "100%",
              mt: 4,
            }}
          >
            <Tooltip title="" placement="right">
              <Typography 
                variant="h4"
                sx={{ 
                  color: "#ffffff",
                  fontWeight: "bold",
                  textTransform: "uppercase",
                  letterSpacing: "1px",
                  fontFamily: "'Roboto Condensed', sans-serif",
                  mb: 2,
                  cursor: "help",
                }}
              >
                Global Category Multipliers
              </Typography>
            </Tooltip>
            <Table size="small">
              <TableHead>
                <TableRow>
                  {carverOrder.map((item) => (
                    <TableCell
                      key={item.key}
                      align="center"
                      sx={{ 
                        color: "#ffffff",
                        fontWeight: "bold",
                        borderBottom: "1px solid rgba(255, 255, 255, 0.1)",
                        padding: 1,
                      }}
                    >
                      <Tooltip title={carverTooltips[item.key]} placement="top">
                        <Box sx={{ 
                          cursor: "help",
                          display: "inline-flex",
                          alignItems: "center",
                          justifyContent: "center"
                        }}>
                          {item.header}
                        </Box>
                      </Tooltip>
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                <TableRow>
                  {carverOrder.map((item) => (
                    <TableCell 
                      key={item.key} 
                      align="center"
                      sx={{
                        borderBottom: "none",
                        padding: 1,
                      }}
                    >
                      <Select
                        value={
                          multipliers[item.key as keyof typeof initialMultipliers]
                        }
                        onChange={multipliersHandleChange(item.key)}
                        size="small"
                        sx={{
                          "& .MuiInputBase-input": { color: "#ffffff" },
                          "& .MuiSelect-icon": { color: "#ffffff" },
                          border: "1px solid rgba(255, 255, 255, 0.23)",
                          borderRadius: "20px",
                          backgroundColor: "rgba(255, 255, 255, 0.05)",
                          '&:hover': {
                            borderColor: '#014093',
                          },
                          '&.Mui-focused': {
                            borderColor: '#014093',
                          },
                          minWidth: "80px",
                        }}
                      >
                        {options.map((option) => (
                          <MenuItem key={option} value={option}>
                            {option.toFixed(1)}x
                          </MenuItem>
                        ))}
                      </Select>
                    </TableCell>
                  ))}
                </TableRow>
              </TableBody>
            </Table>
          </Box>
        </Box>
      </Box>

      {/* Combined Targets & Collaborators in one box (side-by-side) */}
      <Box
        id="targetsAndCollaboratorsBox"
        sx={{
          backgroundColor: "rgba(255, 255, 255, 0.05)",
          backdropFilter: "blur(10px)",
          border: "1px solid rgba(255, 255, 255, 0.1)",
          p: 2,
          borderRadius: 2,
          width: "45%",
          height: "85%",
          display: "flex",
          flexDirection: "row",
          gap: 2,
          overflow: "auto",
          position: "relative",
          zIndex: 1,
        }}
      >
        {/* Targets Column */}
        <Box
          sx={{
            flex: 1,
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              mb: 2,
            }}
          >
            <Tooltip title="" placement="right">
              <Typography 
                variant="h4"
                sx={{ 
                  color: "#ffffff",
                  fontWeight: "bold",
                  textTransform: "uppercase",
                  letterSpacing: "1px",
                  fontFamily: "'Roboto Condensed', sans-serif",
                  cursor: "help",
                }}
              >
                Targets
              </Typography>
            </Tooltip>
            <Button
              variant="contained"
              sx={{ 
                borderRadius: "20px", 
                width: "100px",
                backgroundColor: "#014093",
                color: "#ffffff",
                textTransform: "uppercase",
                fontWeight: "bold",
                letterSpacing: "1px",
                '&:hover': {
                  backgroundColor: "#012B61",
                },
              }}
              onClick={handleAddTarget}
            >
              Add
            </Button>
          </Box>
          {targets.map((target, index) => (
            <Paper
              key={index}
              elevation={0}
              sx={{
                color: "#ffffff",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: 1,
                border: "1px solid rgba(255, 255, 255, 0.23)",
                borderRadius: "20px",
                backgroundColor: "rgba(255, 255, 255, 0.05)",
                backdropFilter: "blur(10px)",
                mb: 1,
                pl: 1,
                height: "56px",
              }}
            >
              <TextField
                value={target}
                onChange={(e) => handleTargetChange(index, e)}
                variant="standard"
                fullWidth
                placeholder="Enter target..."
                InputProps={{
                  disableUnderline: false,
                  style: { fontSize: "1rem", color: "#ffffff" },
                }}
                sx={{ 
                  flexGrow: 1,
                  '& .MuiInput-root': {
                    color: '#ffffff',
                    '&:before': {
                      borderBottomColor: 'rgba(255, 255, 255, 0.23)',
                    },
                    '&:hover:before': {
                      borderBottomColor: '#014093',
                    },
                    '&.Mui-focused:before': {
                      borderBottomColor: '#014093',
                    },
                  },
                  '& .MuiInputBase-input::placeholder': {
                    color: 'rgba(255, 255, 255, 0.5)',
                  },
                }}
                
              />
              <IconButton 
                onClick={() => handleDeleteTarget(index)}
                sx={{
                  color: "rgba(255, 255, 255, 0.7)",
                  '&:hover': {
                    color: "#014093",
                  },
                }}
              >
                <DeleteIcon />
              </IconButton>
            </Paper>
          ))}
        </Box>

        {/* Collaborators Column */}
        <Box
          sx={{
            flex: 1.25,
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              mb: 2,
            }}
          >
            <Tooltip title="" placement="right">
              <Typography 
                variant="h4"
                sx={{ 
                  color: "#ffffff",
                  fontWeight: "bold",
                  textTransform: "uppercase",
                  letterSpacing: "1px",
                  fontFamily: "'Roboto Condensed', sans-serif",
                  cursor: "help",
                }}
              >
                Collaborators
              </Typography>
            </Tooltip>
            <Button
              variant="contained"
              sx={{ 
                borderRadius: "20px", 
                width: "100px",
                backgroundColor: "#014093",
                color: "#ffffff",
                textTransform: "uppercase",
                fontWeight: "bold",
                letterSpacing: "1px",
                '&:hover': {
                  backgroundColor: "#012B61",
                },
              }}
              onClick={handleAddParticipant}
            >
              Invite
            </Button>
          </Box>
          {participantsData.map((participant, index) => (
            <Paper
              key={index}
              elevation={0}
              sx={{
                color: "#ffffff",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: 1,
                border: "1px solid rgba(255, 255, 255, 0.23)",
                borderRadius: "20px",
                backgroundColor: "rgba(255, 255, 255, 0.05)",
                backdropFilter: "blur(10px)",
                mb: 1,
                pl: 1,
                height: "56px",
              }}
            >
              <TextField
                value={participant.email}
                onChange={(e) => handleParticipantEmailChange(index, e)}
                variant="standard"
                placeholder="Enter email..."
                InputProps={{
                  disableUnderline: false,
                  style: { fontSize: "1rem", color: "#ffffff" },
                }}
                sx={{
                  flexGrow: 1,
                  '& .MuiInput-root': {
                    color: '#ffffff',
                    '&:before': {
                      borderBottomColor: 'rgba(255, 255, 255, 0.23)',
                    },
                    '&:hover:before': {
                      borderBottomColor: '#ffffff',
                    },
                    '&.Mui-focused:before': {
                      borderBottomColor: '#014093',
                    },
                  },
                  '& .MuiInputBase-input::placeholder': {
                    color: 'rgba(255, 255, 255, 0.5)',
                  },
                }}
              />
              <Select
                value={participant.role}
                onChange={(e) => handleParticipantRoleChange(index, e)}
                sx={{
                  color: "#ffffff",
                  backgroundColor: "rgba(255, 255, 255, 0.05)",
                  height: "40px",
                  '& .MuiSelect-icon': {
                    color: '#ffffff',
                  },
                  '& fieldset': {
                    borderColor: 'rgba(255, 255, 255, 0.23)',
                  },
                  '&:hover fieldset': {
                    borderColor: '#014093',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#014093',
                  },
                }}
              >
                <MenuItem value="Participant">Participant</MenuItem>
                <MenuItem value="Host">Host</MenuItem>
                <MenuItem value="Host and Participant">Host and Participant</MenuItem>
              </Select>
              <IconButton 
                onClick={() => handleDeleteParticipant(index)}
                sx={{
                  color: "rgba(255, 255, 255, 0.7)",
                  '&:hover': {
                    color: "#014093",
                  },
                }}
              >
                <DeleteIcon />
              </IconButton>
            </Paper>
          ))}
        </Box>
      </Box>
    </Box>
  );
};

export default CreateMatrix;

