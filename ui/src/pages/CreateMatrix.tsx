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
  CircularProgress,
} from "@mui/material";
import { useState } from "react";
import { IconButton } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import { whoamiUpsert, createMatrix } from "./apiService";
import SendIcon from "@mui/icons-material/Send";
import { useNavigate } from "react-router-dom";
import GroupsIcon from '@mui/icons-material/Groups';
import PlaceIcon from '@mui/icons-material/Place';

export const CreateMatrix: React.FC = () => {
  const [RoleBasedChecked, setRoleBasedChecked] = useState(true);
  const [dataEntryMethod, setDataEntryMethodRandom] = useState(false);
  const [value, setValue] = useState<number>(5);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const navigate = useNavigate();
  const [targets, setTargets] = useState<string[]>([]);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarErrorOpen, setSnackbarErrorOpen] = useState(false);
  const [titleError, setTitleError] = useState(false);
  const [snackbarTargetErrorOpen, setSnackbarTargetErrorOpen] = useState(false);
  const [snackbarTargetErrorCharsOpen, setSnackbarTargetErrorCharsOpen] = useState(false);
  const [snackbarCollaboratorsEmailsOpen, setSnackbarCollaboratorsEmailsOpen] = useState(false);
  const [snackbarDuplicateEmailsOpen, setSnackbarDuplicateEmailsOpen] = useState(false);
  const [snackbarParticipantErrorOpen, setSnackbarParticipantErrorOpen] = useState(false);
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
      { email: "", role: RoleBasedChecked ? "Participant" : "Host and Participant" },
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
    const newValue = event.target.checked;
    setRoleBasedChecked(newValue);
    
    // Update all existing participants to "Host and Participant" when disabling role restrictions
    if (!newValue) {
      setParticipantsData(participantsData.map(participant => ({
        ...participant,
        role: "Host and Participant"
      })));
    }
  };

  const dataEntryHandleChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setDataEntryMethodRandom(event.target.checked);
  };

  const scoreRangeHandleChange = (event: SelectChangeEvent<number>) => {
    setValue(event.target.value as number);
  };

  const handleCreateMatrix = async () => {
    try {
      // Duplicate check for targets
      const trimmedTargets = targets.map((target) => target.trim());

      // Check for empty targets
      const hasEmptyTarget = trimmedTargets.some((target) => target === "");
      if (hasEmptyTarget) {
        setSnackbarTargetErrorCharsOpen(true);
        return;
      }

      // Check for empty emails
      const hasEmptyEmail = participantsData.some((p) => p.email.trim() === "");
      if (hasEmptyEmail) {
        setSnackbarCollaboratorsEmailsOpen(true);
        return;
      }

      // Check for duplicate emails
      const emails = participantsData.map(p => p.email.trim().toLowerCase());
      const uniqueEmails = new Set(emails);

      if (uniqueEmails.size !== emails.length)
      {
        setSnackbarDuplicateEmailsOpen(true);
        return;
      }

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

      // Check for at least one participant (either Participant or Host and Participant role)
      const participantCount = participantsData.filter(
        p => p.role === "Participant" || p.role === "Host and Participant"
      ).length;

      if (participantCount === 0) {
        setSnackbarParticipantErrorOpen(true);
        return;
      }

      const whoamiUpsertResponse = await whoamiUpsert();
      const rawData = whoamiUpsertResponse.data;
      const firstObjectEnd = rawData.indexOf("}") + 1;
      const firstObjectStr = rawData.substring(0, firstObjectEnd);
      const parsedFirstObject = JSON.parse(firstObjectStr);
      const { userId, email } = parsedFirstObject;

      const items = targets.map((target) => ({
        itemName: target,
        criticality: {},
        accessibility: {},
        recoverability: {},
        vulnerability: {},
        effect: {},
        recognizability: {}
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
        randomAssignment: dataEntryMethod,
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
        }, 1500);
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
        flexDirection: "column",
        alignItems: "center",
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

      {/* Main content container */}
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          width: "100%",
          height: "100vh",
          pt: 1,
          gap: 2,
          overflow: "hidden",
        }}
      >
        {/* Title Box and Create Button Container */}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            width: "100%",
            gap: 1.5,
            pl: 0,
            pr: 2,
          }}
        >
          <Box
            sx={{
              backgroundColor: "rgba(255, 255, 255, 0.05)",
              backdropFilter: "blur(10px)",
              border: "1px solid rgba(255, 255, 255, 0.1)",
              p: 2,
              borderRadius: 2,
              flex: 0.96,
              display: "flex",
              alignItems: "center",
              position: "relative",
              zIndex: 1,
            }}
          >
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
          </Box>
          <Button
            variant="contained"
            onClick={handleCreateMatrix}
            endIcon={<SendIcon />}
            sx={{ 
              borderRadius: "20px", 
              width: "140px",
              height: "55px",
              backgroundColor: "#014093",
              color: "#ffffff",
              textTransform: "uppercase",
              fontWeight: "bold",
              letterSpacing: "0.5px",
              fontSize: "0.9rem",
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
              flexShrink: 0,
              '&:hover': {
                backgroundColor: "#012B61",
                boxShadow: '0 6px 8px rgba(0, 0, 0, 0.2)'
              },
            }}
          >
            Create Matrix
          </Button>
        </Box>

        {/* Content Container */}
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            width: "96.5%",
            flex: 1,
            gap: 2,
            pl: 0,
            pr: 0,
            pb: 2,
            maxWidth: "96.5%",
            margin: "0 auto",
            overflow: "hidden",
          }}
        >
          {/* Left section: Matrix parameters and multipliers */}
          <Box
            id="matrixCreatorBox"
            sx={{
              backgroundColor: "rgba(255, 255, 255, 0.05)",
              backdropFilter: "blur(10px)",
              border: "1px solid rgba(255, 255, 255, 0.1)",
              p: 2,
              borderRadius: 2,
              flex: 1,
              height: "70vh",
              display: "flex",
              flexDirection: "column",
              overflow: "auto",
              position: "relative",
              zIndex: 1,
              minWidth: 0, // Prevent flex item from overflowing
            }}
          >
            <TextField
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              multiline
              minRows={3}
              maxRows={5}
              variant="outlined"
              fullWidth
              placeholder="Enter Matrix description..."
              sx={{ 
                '& .MuiOutlinedInput-root': {
                  color: '#ffffff',
                  '& fieldset': {
                    borderColor: 'rgba(255, 255, 255, 0.23)',
                  },
                  '&:hover fieldset': {
                    borderColor: '#ffffff',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#014093',
                  },
                  '& textarea': {
                    fontSize: '0.95rem',
                    lineHeight: '1.5',
                  },
                },
                '& .MuiInputBase-input::placeholder': {
                  color: 'rgba(255, 255, 255, 0.5)',
                  fontSize: '0.95rem',
                },
                mb: 2,
              }}
            />
            <Box
              id="matrixParametersBox"
              sx={{
                width: "100%",
                mt: 3,
                display: "flex",
                flexDirection: "column",
                marginTop: "20px",
              }}
            >
              <Tooltip title="Define the configuration options for your CARVER matrix, such as scoring scale, role restrictions, and how data entry is assigned." placement="top">
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
                    <Tooltip title="Select whether the matrix uses a 5-point or 10-point scale to evaluate each CARVER criterion." placement="top">
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
                  <Tooltip title="Toggle whether collaborators have different permissions based on their role (e.g., Hosts vs. Participants)." placement="top">
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
                    <Tooltip title="Choose whether data entry tasks are randomly distributed or shown to all collaborators." placement="top">
                      <FormLabel 
                        id="data-entry-label"
                        sx={{ color: "rgba(255, 255, 255, 0.7)", cursor: "help" }}
                      >
                        Random Target Distribution
                      </FormLabel>
                    </Tooltip>
                    <FormControlLabel
                    control={
                      <Switch
                        checked={dataEntryMethod}
                        onChange={dataEntryHandleChange}
                        sx={{
                          "& .MuiSwitch-track": {
                            borderRadius: "20px",
                            backgroundColor: dataEntryMethod ? "#014093" : "rgba(255, 255, 255, 0.23)",
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
                        {dataEntryMethod ? "On" : "Off"}
                      </Typography>
                    }
                    sx={{ mb: 1 }}
                  />
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
                <Tooltip title="Adjust the relative importance of each CARVER category globally. These multipliers affect how much weight each factor has in final scoring." placement="top">
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

          {/* Targets Box */}
          <Box
            sx={{
              display: "flex",
              flexDirection: "column",
              gap: 1,
              flex: 1,
              height: "73.75vh",
              justifyContent: "space-between",
              minWidth: 0, // Prevent flex item from overflowing
            }}
          >
            <Box
              id="targetsBox"
              sx={{
                backgroundColor: "rgba(255, 255, 255, 0.05)",
                backdropFilter: "blur(10px)",
                border: "1px solid rgba(255, 255, 255, 0.1)",
                p: 2,
                flex: 0.49,
                display: "flex",
                flexDirection: "column",
                gap: 2,
                overflow: "auto",
                position: "relative",
                zIndex: 1,
                borderRadius: 2,
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
                <Tooltip title="Define the specific assets, systems, or entities to be evaluated in your CARVER matrix." placement="top">
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
                    <PlaceIcon sx={{ marginRight: "10px" , fontSize: "1.5rem" }} />
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
              {targets.length === 0 && (
                <Typography sx={{ color: 'rgba(255,255,255,0.4)', fontStyle: 'italic', mt: 1 }}>
                  No targets added yet. Click "Add" to get started.
                </Typography>
              )}
              {targets.map((target, index) => (
                <Paper
                  key={index}
                  elevation={0}
                  sx={{
                    color: "#ffffff",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    gap: 2,
                    border: "1px solid rgba(255, 255, 255, 0.12)",
                    borderRadius: "12px",
                    backgroundColor: "rgba(255, 255, 255, 0.03)",
                    backdropFilter: "blur(10px)",
                    mb: 1,
                    pl: 2,
                    pr: 1,
                    height: "50px",
                    transition: "all 0.2s ease-in-out",
                    '&:hover': {
                      backgroundColor: "rgba(255, 255, 255, 0.07)",
                      borderColor: "rgba(255, 255, 255, 0.2)",
                      transform: "translateX(4px)",
                    }
                  }}
                >
                  <TextField
                    value={target}
                    onChange={(e) => handleTargetChange(index, e)}
                    variant="standard"
                    fullWidth
                    placeholder="Enter target..."
                    InputProps={{
                      disableUnderline: true,
                      style: { 
                        fontSize: "0.95rem", 
                        color: "#ffffff",
                        fontWeight: "500",
                      },
                    }}
                    sx={{ 
                      flexGrow: 1,
                      '& .MuiInputBase-root': {
                        padding: "4px 0",
                      },
                      '& .MuiInputBase-input::placeholder': {
                        color: 'rgba(255, 255, 255, 0.4)',
                        fontWeight: "400",
                      },
                    }}
                  />
                  <IconButton 
                    onClick={() => handleDeleteTarget(index)}
                    sx={{
                      color: "rgba(255, 255, 255, 0.5)",
                      padding: "6px",
                      transition: "all 0.2s ease",
                      '&:hover': {
                        color: "#ff4444",
                        backgroundColor: "rgba(255, 68, 68, 0.08)",
                      },
                    }}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Paper>
              ))}
            </Box>

            {/* Collaborators Box */}
            <Box
              id="collaboratorsBox"
              sx={{
                backgroundColor: "rgba(255, 255, 255, 0.05)",
                backdropFilter: "blur(10px)",
                border: "1px solid rgba(255, 255, 255, 0.1)",
                p: 2,
                flex: 0.49,
                display: "flex",
                flexDirection: "column",
                gap: 2,
                overflow: "auto",
                position: "relative",
                zIndex: 1,
                borderRadius: 2,
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
                <Tooltip title="Invite users to participate in the matrix evaluation process, assigning them roles to control their level of access and responsibility." placement="top">
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
                    <GroupsIcon sx={{ marginRight: "10px" , fontSize: "1.5rem" }} />
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
              {participantsData.length === 0 && (
                <Typography sx={{ color: 'rgba(255,255,255,0.4)', fontStyle: 'italic', mt: 1 }}>
                  No collaborators added yet. Click "Invite" to add participants.
                </Typography>
              )}
              {participantsData.map((participant, index) => (
                <Paper
                  key={index}
                  elevation={0}
                  sx={{
                    color: "#ffffff",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    gap: 2,
                    border: "1px solid rgba(255, 255, 255, 0.12)",
                    borderRadius: "12px",
                    backgroundColor: "rgba(255, 255, 255, 0.03)",
                    backdropFilter: "blur(10px)",
                    mb: 1,
                    pl: 2,
                    pr: 1,
                    height: "50px",
                    transition: "all 0.2s ease-in-out",
                    '&:hover': {
                      backgroundColor: "rgba(255, 255, 255, 0.07)",
                      borderColor: "rgba(255, 255, 255, 0.2)",
                      transform: "translateX(4px)",
                    }
                  }}
                >
                  <TextField
                    value={participant.email}
                    onChange={(e) => handleParticipantEmailChange(index, e)}
                    variant="standard"
                    placeholder="Enter email..."
                    InputProps={{
                      disableUnderline: true,
                      style: { 
                        fontSize: "0.95rem", 
                        color: "#ffffff",
                        fontWeight: "500",
                      },
                    }}
                    sx={{
                      flexGrow: 1,
                      '& .MuiInputBase-root': {
                        padding: "4px 0",
                      },
                      '& .MuiInputBase-input::placeholder': {
                        color: 'rgba(255, 255, 255, 0.4)',
                        fontWeight: "400",
                      },
                    }}
                  />
                  {RoleBasedChecked && (
                    <Select
                      value={participant.role}
                      onChange={(e) => handleParticipantRoleChange(index, e)}
                      size="small"
                      sx={{
                        color: "#ffffff",
                        backgroundColor: "rgba(255, 255, 255, 0.05)",
                        height: "32px",
                        minWidth: "140px",
                        fontSize: "0.9rem",
                        '& .MuiSelect-select': {
                          padding: "4px 12px",
                        },
                        '& .MuiSelect-icon': {
                          color: '#ffffff',
                        },
                        '& fieldset': {
                          borderColor: 'rgba(255, 255, 255, 0.15)',
                          borderRadius: "8px",
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
                  )}
                  <IconButton 
                    onClick={() => handleDeleteParticipant(index)}
                    sx={{
                      color: "rgba(255, 255, 255, 0.5)",
                      padding: "6px",
                      transition: "all 0.2s ease",
                      '&:hover': {
                        color: "#ff4444",
                        backgroundColor: "rgba(255, 68, 68, 0.08)",
                      },
                    }}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Paper>
              ))}
            </Box>
          </Box>
        </Box>
      </Box>

      {/* Snackbars */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        sx={{
          '& .MuiPaper-root': {
            width: '100%',
            maxWidth: '400px',
          }
        }}
      >
        <Alert
          onClose={undefined}
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
            width: '100%',
            '& .MuiAlert-message': {
              width: '100%',
              padding: '8px 0',
              overflow: 'hidden'
            },
            '& .MuiAlert-action': {
              display: 'none'
            }
          }}
        >
          <Box sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 2,
            width: '100%',
            overflow: 'hidden'
          }}>
            <Box sx={{ 
              flex: 1,
              overflow: 'hidden',
              minWidth: 0
            }}>
              <Typography 
                variant="body1" 
                sx={{ 
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap'
                }}
              >
                Matrix Created Successfully!
              </Typography>
              <Typography 
                variant="body2" 
                sx={{ 
                  opacity: 0.9,
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap'
                }}
              >
                Redirecting to Matrix Explorer
              </Typography>
            </Box>
            <Box sx={{ flexShrink: 0, display: 'flex', alignItems: 'center' }}>
              <CircularProgress
                size={20}
                sx={{
                  color: '#ffffff'
                }}
              />
            </Box>
          </Box>
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
        open={snackbarTargetErrorCharsOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarTargetErrorCharsOpen(false)}
      >
        <Alert
          onClose={() => setSnackbarTargetErrorCharsOpen(false)}
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
          Each target must have at least 1 character
        </Alert>
      </Snackbar>

      <Snackbar
        open={snackbarCollaboratorsEmailsOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarCollaboratorsEmailsOpen(false)}
      >
        <Alert
          onClose={() => setSnackbarCollaboratorsEmailsOpen(false)}
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
          Cannot have empty email
        </Alert>
      </Snackbar>

      <Snackbar
        open={snackbarDuplicateEmailsOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarDuplicateEmailsOpen(false)}
      >
        <Alert
          onClose={() => setSnackbarDuplicateEmailsOpen(false)}
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
          Cannot have duplicate emails
        </Alert>
      </Snackbar>

      <Snackbar
        open={snackbarParticipantErrorOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarParticipantErrorOpen(false)}
      >
        <Alert
          onClose={() => setSnackbarParticipantErrorOpen(false)}
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
          Must have at least one user with Participant role
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default CreateMatrix;

