include(ExternalProject)

ExternalProject_Add(ep_webp
        SOURCE_DIR ${THIRD_PARTY_SOURCE_PATH}/libwebp
        CMAKE_ARGS
            ${EP_CMAKE_ARGS}
            -DWEBP_BUILD_VWEBP=OFF
            -DWEBP_LINK_STATIC=ON
            # libwebp's extras/extras.h declares SharpYuvRiskThresholds via
            # WEBP_EXTERN, which on MinGW (_WIN32 + WEBP_DLL) expands to
            # __declspec(dllexport) WITHOUT extern: a tentative definition
            # in every TU that includes it. GCC 10+ defaults to -fno-common,
            # so webp_quality.exe fails to link with "multiple definition".
            # -fcommon restores classic merging for this dep only.
            -DCMAKE_C_FLAGS=-fcommon
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)